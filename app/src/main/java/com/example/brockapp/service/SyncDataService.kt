package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.extraObject.MySharedPreferences

import java.io.File
import android.util.Log
import android.os.IBinder
import android.app.Service
import com.google.gson.Gson
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import androidx.work.OneTimeWorkRequestBuilder
import com.amazonaws.services.s3.AmazonS3Client
import com.example.brockapp.worker.SyncDataWorker
import com.amazonaws.services.s3.model.PutObjectRequest

class SyncDataService: Service() {
    private lateinit var db: BrockDB
    private lateinit var file: File
    private lateinit var s3Client: AmazonS3Client

    override fun onCreate() {
        super.onCreate()

        db = BrockDB.getInstance(this)
        file = File(this.filesDir, "user_data.json")
        s3Client = MyS3ClientProvider.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (MySharedPreferences.checkService("DUMP_DATABASE", applicationContext)) {
            syncData()
            sendNotification(applicationContext)
        } else {
            Log.d("SYNC_DATA_SERVICE", "Permission dump database denied")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun syncData() {
        if (MyNetwork.isConnected && MySharedPreferences.checkService("DUMP_DATABASE", this)) {
            CoroutineScope(Dispatchers.IO).launch {
                val geofence = db
                    .GeofenceTransitionDao()
                    .getAllGeofenceTransitionByUserId(MyUser.id)

                val walkActivities = db
                    .UserWalkActivityDao()
                    .getWalkActivitiesByUserId(MyUser.id)

                val vehicleActivities = db
                    .UserVehicleActivityDao()
                    .getVehicleActivitiesByUserId(MyUser.id)

                val stillActivities = db
                    .UserStillActivityDao()
                    .getStillActivitiesByUserId(MyUser.id)

                val userData = mapOf(
                    "username" to MyUser.username,
                    "walkActivities" to walkActivities,
                    "vehicleActivities" to vehicleActivities,
                    "stillActivities" to stillActivities,
                    "geofenceTransitions" to geofence
                )

                withContext(Dispatchers.Default) {
                    val gson = Gson()
                    val json = gson.toJson(userData)

                    file.writeText(json)
                }

                try {
                    val request =
                        PutObjectRequest(BUCKET_NAME, "user/${MyUser.username}.json", file)
                    s3Client.putObject(request)
                } catch (e: Exception) {
                    Log.e("SYNC_DATA_SERVICE", "Failed to upload user data $e")
                }
            }
        }
    }

    private fun sendNotification(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<SyncDataWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}