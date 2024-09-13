package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

import android.util.Log
import java.time.Instant
import android.os.IBinder
import android.app.Service
import java.time.ZoneOffset
import android.content.Intent
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService: Service() {
    private lateinit var db: BrockDB

    override fun onCreate() {
        super.onCreate()
        db = BrockDB.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val activityType = intent.getIntExtra("ACTIVITY_TYPE", -1)
            val transitionType = intent.getIntExtra("TRANSITION_TYPE", -1)
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
                                    userId = User.id,
                                    transitionType = transitionType,
                                    timestamp = timestamp
                                )
                            )
                        }

                        DetectedActivity.IN_VEHICLE -> {
                            val distanceTravelled =
                                intent.getDoubleExtra("DISTANCE_TRAVELLED", -1.0)

                            userVehicleActivityDao.insertVehicleActivity(
                                UserVehicleActivityEntity(
                                    userId = User.id,
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
                                    userId = User.id,
                                    transitionType = transitionType,
                                    timestamp = timestamp,
                                    stepNumber = stepNumber
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ACTIVITY_SERVICE", e.toString())
                }
            }
        } else {
            Log.d("ACTIVITY_SERVICE", "Null intent")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}