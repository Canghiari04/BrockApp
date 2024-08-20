package com.example.brockapp.service

import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

import android.util.Log
import android.os.IBinder
import android.app.Service
import android.content.Intent
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService : Service() {
    private lateinit var user: User
    private lateinit var db: BrockDB
    private lateinit var util: NotificationUtil

    override fun onCreate() {
        super.onCreate()
        user = User.getInstance()
        util = NotificationUtil()
        db = BrockDB.getInstance(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val activityType = intent.getIntExtra("ACTIVITY_TYPE", -1)
        val transitionType = intent.getIntExtra("TRANSITION_TYPE", -1)
        val timestamp = intent.getStringExtra("TIMESTAMP")

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
                        val distanceTravelled = intent.getDoubleExtra("DISTANCE_TRAVELLED", -1.0)

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
                        val stepNumber = intent.getLongExtra("STEP_NUMBER", -1)

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

                if (transitionType == 1) {
                    sendActivityNotify(activityType)
                } else {
                    Log.d("ACTIVITY_NOTIFICATION", "Notify not request.")
                }
            } catch (e: Exception) {
                Log.d("ACTIVITY_NOTIFICATION", e.toString())
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendActivityNotify(type: Int) {
        val intent = util.getActivityRecognitionIntent(type)
        sendBroadcast(intent)
    }
}