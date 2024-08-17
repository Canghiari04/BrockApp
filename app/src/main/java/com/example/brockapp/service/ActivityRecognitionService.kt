package com.example.brockapp.service

import com.example.brockapp.singleton.User
import com.example.brockapp.ISO_DATE_FORMAT
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE
import com.example.brockapp.database.UserVehicleActivityEntity

import android.util.Log
import java.time.Instant
import android.os.IBinder
import android.app.Service
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import android.content.IntentFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import android.content.BroadcastReceiver
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService : Service() {
    private lateinit var receiver: BroadcastReceiver
    private lateinit var user: User
    private lateinit var db: BrockDB
    private lateinit var utilNotification: NotificationUtil

    override fun onCreate() {
        super.onCreate()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == ACTIVITY_RECOGNITION_INTENT_TYPE) {
                    user = User.getInstance()
                    db = BrockDB.getInstance(context)
                    utilNotification = NotificationUtil()

                    val activityType = intent.getIntExtra("activityType", -1)
                    val transitionType = intent.getIntExtra("transitionType", -1)
                    val timestamp = DateTimeFormatter
                        .ofPattern(ISO_DATE_FORMAT)
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now())

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val userStillActivityDao = db.UserStillActivityDao()
                            val userVehicleActivityDao = db.UserVehicleActivityDao()
                            val userWalkActivityDao = db.UserWalkActivityDao()

                            when (activityType) {
                                DetectedActivity.STILL -> {
                                    userStillActivityDao.insertStillActivity(
                                        UserStillActivityEntity(
                                            userId = user.id,
                                            transitionType = transitionType,
                                            timestamp = timestamp
                                        )
                                    )
                                }

                                DetectedActivity.IN_VEHICLE -> {
                                    val distanceTravelled = intent.getDoubleExtra("distanceTravelled", -1.0)
                                    userVehicleActivityDao.insertVehicleActivity(
                                        UserVehicleActivityEntity(
                                            userId = user.id,
                                            transitionType = transitionType,
                                            timestamp = timestamp,
                                            distanceTravelled = distanceTravelled
                                        )
                                    )
                                }

                                DetectedActivity.WALKING -> {
                                    val stepNumber = intent.getLongExtra("stepNumber", -1)
                                    userWalkActivityDao.insertWalkActivity(
                                        UserWalkActivityEntity(
                                            userId = user.id,
                                            transitionType = transitionType,
                                            timestamp = timestamp,
                                            stepNumber = stepNumber
                                        )
                                    )
                                }
                            }

                            sendActivityNotify(activityType)
                        } catch (e: Exception) {
                            Log.d("ACTIVITY_RECOGNITION_DATABASE", e.toString())
                        }
                    }
                }
            }
        }

        registerReceiver(receiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT_TYPE))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun sendActivityNotify(type: Int) {
        val intent = utilNotification.getActivityRecognitionIntent(type)
        sendBroadcast(intent)
    }
}