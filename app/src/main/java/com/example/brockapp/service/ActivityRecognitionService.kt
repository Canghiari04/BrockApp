package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

import android.util.Log
import java.time.Instant
import android.os.IBinder
import android.app.Service
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import android.content.ComponentName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import android.content.ServiceConnection
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService: Service() {
    private var isBoundWalkService = false
    private var isBoundVehicleService = false
    private var walkService: WalkService? = null
    private var vehicleService: VehicleService? = null

    private lateinit var db: BrockDB

    private val vehicleServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // When connection is established I can retrieve the service to get the distance traveled
            val binder = service as VehicleService.LocalBinder
            vehicleService = binder.getService()

            isBoundVehicleService = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBoundVehicleService = false
        }
    }

    private val walkServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // I retrieve the service from thr binder, then I will take the step done
            val binder = service as WalkService.LocalBinder

            walkService = binder.getService()
            isBoundWalkService = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBoundWalkService = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        db = BrockDB.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            // Sync automatic when new activity are inserted
            // viewModelFriends.uploadUserData()

            // If activity type not found, I will put the UNKNOWN type
            val activityType = intent.getIntExtra("ACTIVITY_TYPE", 4)
            val transitionType = intent.getIntExtra("TRANSITION_TYPE", 0)
            val arrivalTime = intent.getLongExtra("ARRIVAL_TIME", 0L)
            val exitTime = intent.getLongExtra("EXIT_TIME", 0L)

            val timeStamp = getInstant()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    insertActivity(activityType, transitionType, timeStamp, arrivalTime, exitTime)
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

    private fun getInstant(): String {
        return DateTimeFormatter
            .ofPattern(ISO_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }

    private suspend fun insertActivity(activityType: Int, transitionType: Int, timeStamp: String, arrivalTime: Long, exitTime: Long) {
        when (activityType) {
            DetectedActivity.STILL -> {
                if (transitionType == 0) {
                    insertStillActivity(timeStamp, arrivalTime, exitTime)
                } else {
                    val lastId = db.UserStillActivityDao().getLastInsertedId()!!
                    db.UserStillActivityDao().updateExitTime(lastId, exitTime)
                }
            }

            DetectedActivity.IN_VEHICLE -> {
                if (transitionType == 0) {
                    val intent = Intent(this, VehicleService::class.java)
                    startService(intent)
                    bindService(intent, vehicleServiceConnection, Context.BIND_AUTO_CREATE)

                    insertVehicleActivity(timeStamp, arrivalTime, exitTime, 0.0)
                } else {
                    if (isBoundVehicleService) {
                        val distanceTraveled = vehicleService?.getDistance()!!
                        unbindService(vehicleServiceConnection)

                        val lastId = db.UserVehicleActivityDao().getLastInsertedId()!!
                        db.UserVehicleActivityDao().updateExitTimeAndDistance(lastId, exitTime, distanceTraveled)
                    } else {
                        Log.e("ACTIVITY_RECOGNITION_SERVICE", "Connection closed unexpectedly")
                    }
                }
            }

            DetectedActivity.WALKING -> {
                if (transitionType == 0) {
                    val intent = Intent(this, WalkService::class.java)
                    startService(intent)
                    bindService(intent, walkServiceConnection, Context.BIND_AUTO_CREATE)

                    insertWalkActivity(timeStamp, arrivalTime, exitTime, 0L)
                } else {
                    if (isBoundWalkService) {
                        val stepNumber = walkService?.getSteps()!!
                        unbindService(walkServiceConnection)

                        val lastId = db.UserWalkActivityDao().getLastInsertedId()!!
                        db.UserWalkActivityDao().updateExitTimeAndSteps(lastId, exitTime, stepNumber)
                    } else {
                        Log.e("ACTIVITY_RECOGNITION_SERVICE", "Connection closed unexpectedly")
                    }
                }
            }
        }
    }

    private suspend fun insertStillActivity(timeStamp: String, arrivalTime: Long, exitTime: Long) {
        db.UserStillActivityDao().insertStillActivity(
            UserStillActivityEntity(
                userId = MyUser.id,
                timestamp = timeStamp,
                arrivalTime = arrivalTime,
                exitTime = exitTime
            )
        )
    }

    private suspend fun insertVehicleActivity(timeStamp: String, arrivalTime: Long, exitTime: Long, distanceTraveled: Double) {
        db.UserVehicleActivityDao().insertVehicleActivity(
            UserVehicleActivityEntity(
                userId = MyUser.id,
                timestamp = timeStamp,
                arrivalTime = arrivalTime,
                exitTime = exitTime,
                distanceTravelled = distanceTraveled
            )
        )
    }

    private suspend fun insertWalkActivity(timeStamp: String, arrivalTime: Long, exitTime: Long, stepNumber: Long) {
        db.UserWalkActivityDao().insertWalkActivity(
            UserWalkActivityEntity(
                userId = MyUser.id,
                timestamp = timeStamp,
                arrivalTime = arrivalTime,
                exitTime = exitTime,
                stepNumber = stepNumber
            )
        )
    }
}