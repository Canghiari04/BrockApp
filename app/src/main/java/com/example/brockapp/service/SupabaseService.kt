package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.room.UsersEntity
import com.example.brockapp.room.FriendsEntity
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.singleton.MySupabase
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.activity.MainActivity
import com.example.brockapp.activity.LoginActivity
import com.example.brockapp.util.ScheduleWorkerUtil
import com.example.brockapp.room.GeofenceAreasEntity
import com.example.brockapp.room.UsersRunActivityEntity
import com.example.brockapp.room.UsersWalkActivityEntity
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.room.UsersStillActivityEntity
import com.example.brockapp.room.GeofenceTransitionsEntity
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.room.UsersVehicleActivityEntity
import com.example.brockapp.extraObject.MySharedPreferences

import java.io.File
import android.util.Log
import android.os.Build
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import android.content.IntentFilter
import kotlinx.coroutines.Dispatchers
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import android.content.BroadcastReceiver
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.SupabaseClient
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.DeleteObjectRequest

class SupabaseService: Service() {

    private var notificationUtil = NotificationUtil()

    private val toastUtil = ShowCustomToastImpl()

    private lateinit var file: File
    private lateinit var room: BrockDB
    private lateinit var s3Client: AmazonS3Client
    private lateinit var supabase: SupabaseClient
    private lateinit var receiver: BroadcastReceiver
    private lateinit var scheduleWorkerUtil: ScheduleWorkerUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        supabase = MySupabase.getInstance()
        room = BrockDB.getInstance(this)

        s3Client = MyS3ClientProvider.getInstance(this)
        file = File(this.filesDir, "user_data.json")

        scheduleWorkerUtil = ScheduleWorkerUtil(this)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "SYNC_DATA_ACTION") {
                    when (intent.getStringExtra("NEXT_ACTIVITY") ?: " ") {
                        NextActivity.LOGIN.toString() -> {
                            Intent(context, LoginActivity::class.java).also {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(it)
                                stopSelf()
                            }
                        }

                        NextActivity.HOME.toString() -> {
                            Intent(context, MainActivity::class.java).also {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(it)
                                stopSelf()
                            }
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter("SYNC_DATA_ACTION")
        registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.SYNC.toString() -> {
                syncSupabase()
            }

            Actions.READ.toString() -> {
                start()
                syncRoom()
            }

            Actions.DELETE.toString() -> {
                deleteAll()
            }

            Actions.TERMINATE.toString() -> {
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    enum class Actions {
        SYNC, READ, DELETE, TERMINATE
    }

    enum class NextActivity {
        LOGIN, HOME
    }

    private fun syncSupabase() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                supabase.apply {
                    val user =  room.UsersDao().getUserFromUsernameAndPassword(MyUser.username, MyUser.password)

                    val runActivities = room.UsersRunActivityDao()
                        .getRunActivitiesByUsername(MyUser.username)
                        .filter { it.distanceDone > 0.0 }

                    val walkActivities = room.UsersWalkActivityDao()
                        .getWalkActivitiesByUsername(MyUser.username)
                        .filter { it.stepsNumber > 0 }

                    val stillActivities = room.UsersStillActivityDao()
                        .getStillActivitiesByUsername(MyUser.username)

                    val vehicleActivities = room.UsersVehicleActivityDao()
                        .getVehicleActivitiesByUsername(MyUser.username)
                        .filter { it.distanceTravelled > 0.0 }

                    val geofenceAreas = room.GeofenceAreasDao()
                        .getGeofenceAreasByUsername(MyUser.username)
                        .filter { it.name.isNotBlank() }

                    val geofenceTransition = room.GeofenceTransitionsDao()
                        .getGeofenceTransitionsByUsername(MyUser.username)
                        .filter { it.nameLocation.isNotBlank() }

                    val memos = room.MemosDao().getMemosByUsername(MyUser.username)

                    val friends = room.FriendsDao().getFriendsByUsername(MyUser.username)

                    supabase.from("Users").upsert(
                        user
                    )

                    supabase.from("UsersVehicleActivity").upsert(
                        vehicleActivities
                    )

                    supabase.from("UsersRunActivity").upsert(
                        runActivities
                    )

                    supabase.from("UsersStillActivity").upsert(
                        stillActivities
                    )

                    supabase.from("UsersWalkActivity").upsert(
                        walkActivities
                    )

                    supabase.from("GeofenceAreas").upsert(
                        geofenceAreas
                    )

                    supabase.from("GeofenceTransitions").upsert(
                        geofenceTransition
                    )

                    supabase.from("Memos").upsert(
                        memos
                    )

                    supabase.from("Friends").upsert(
                        friends
                    )
                }

                room.UsersDao().deleteUser(
                    MyUser.username,
                    MyUser.password
                )

                MySharedPreferences.deleteSavedPreferences(applicationContext)

                scheduleWorkerUtil.also {
                    it.deleteSyncPeriodic()

                    it.deleteMemoWorker()
                    it.deleteActivityWorker()
                    it.deleteGeofenceAreaWorker()
                }

                Intent().also {
                    it.action = "SYNC_DATA_ACTION"
                    it.putExtra("NEXT_ACTIVITY", NextActivity.LOGIN.toString())

                    sendBroadcast(it)
                }
            }
        } catch (e: Exception) {
            Intent().also {
                it.action = "SYNC_DATA_ACTION"
                it.putExtra("NEXT_ACTIVITY", NextActivity.HOME.toString())

                toastUtil.showWarningToast(
                    "This action cannot be done",
                    applicationContext
                )

                sendBroadcast(it)
            }
        }
    }

    private fun start() {
        startForeground(
            ID_SUPABASE_SERVICE_NOTIFY,
            notificationUtil.getNotificationBody(
                CHANNEL_ID_SUPABASE_SERVICE,
                R.drawable.icon_sync,
                "BrockApp - Sync to service",
                "The app is being configured, please wait a few seconds",
                this
            ).build()
        )
    }

    private fun syncRoom() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val user = supabase.from("Users").select {
                    filter { eq("username", MyUser.username) }
                }.decodeSingle<UsersEntity>()

                val vehicleActivities = supabase.from("UsersVehicleActivity").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<UsersVehicleActivityEntity>()

                val runActivities = supabase.from("UsersRunActivity").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<UsersRunActivityEntity>()

                val stillActivities = supabase.from("UsersStillActivity").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<UsersStillActivityEntity>()

                val walkActivities = supabase.from("UsersWalkActivity").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<UsersWalkActivityEntity>()

                val geofenceAreas = supabase.from("GeofenceAreas").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<GeofenceAreasEntity>()

                val geofenceTransitions = supabase.from("GeofenceTransitions").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<GeofenceTransitionsEntity>()

                val memos = supabase.from("Memos").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<MemosEntity>()

                val friends = supabase.from("Friends").select {
                    filter { eq("username", MyUser.username) }
                }.decodeList<FriendsEntity>()

                room.UsersDao().deleteUser(
                    user.username,
                    user.password
                )

                room.UsersDao().insertUser(user)

                room.UsersVehicleActivityDao().insertVehicleActivities(vehicleActivities)
                room.UsersRunActivityDao().insertRunActivities(runActivities)
                room.UsersStillActivityDao().insertStillActivities(stillActivities)
                room.UsersWalkActivityDao().insertWalkActivities(walkActivities)

                room.GeofenceAreasDao().insertGeofenceAreas(geofenceAreas)
                room.GeofenceTransitionsDao().insertGeofenceTransitions(geofenceTransitions)

                room.MemosDao().insertMemos(memos)

                room.FriendsDao().insertFriends(friends)

                Intent().also {
                    it.putExtra("NEXT_ACTIVITY", NextActivity.HOME.toString())
                    it.action = "SYNC_DATA_ACTION"
                    sendBroadcast(it)
                }
            }
        } catch (e: Exception) {
            Intent().also {
                it.putExtra("NEXT_ACTIVITY", NextActivity.LOGIN.toString())
                it.action = "SYNC_DATA_ACTION"
                sendBroadcast(it)
            }
        }
    }

    private fun deleteAll() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                room.UsersDao().deleteUser(
                    MyUser.username,
                    MyUser.password
                )

                supabase.from("Users").delete {
                    filter {
                        eq("username", MyUser.username)
                    }
                    filter {
                        eq("password", MyUser.password)
                    }
                }

                try {
                    val request =
                        DeleteObjectRequest(BuildConfig.BUCKET_NAME, "user/${MyUser.username}.json")
                    s3Client.deleteObject(request)
                } catch (e: Exception) {
                    Log.d("SUPABASE_SERVICE", "User does not have the dump on S3")
                }

                scheduleWorkerUtil.also {
                    it.deleteMemoWorker()
                    it.deleteSyncPeriodic()
                    it.deleteActivityWorker()
                    it.deleteGeofenceAreaWorker()
                }

                Intent().also {
                    it.action = "SYNC_DATA_ACTION"
                    it.putExtra("NEXT_ACTIVITY", NextActivity.LOGIN.toString())

                    MySharedPreferences.deleteSavedPreferences(applicationContext)
                    sendBroadcast(it)
                }
            }
        } catch (e: Exception) {
            Intent().also {
                it.action = "SYNC_DATA_ACTION"
                it.putExtra("NEXT_ACTIVITY", NextActivity.HOME.toString())

                toastUtil.showWarningToast(
                    "This action cannot be done",
                    applicationContext
                )

                sendBroadcast(it)
            }
        }
    }
}