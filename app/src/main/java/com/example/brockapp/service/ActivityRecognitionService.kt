package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.room.UsersRunActivityEntity
import com.example.brockapp.room.UsersWalkActivityEntity
import com.example.brockapp.room.UsersStillActivityEntity
import com.example.brockapp.room.UsersVehicleActivityEntity
import com.example.brockapp.singleton.MyActivityRecognition
import com.example.brockapp.extraObject.MyServiceConnection

import android.util.Log
import java.time.Instant
import android.os.IBinder
import android.app.Service
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import android.content.ServiceConnection
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService: Service() {
    private var isDistanceServiceBound = false
    private var isStepCounterServiceBound = false
    private var isHeightDifferenceServiceBound = false
    private var distanceService: DistanceService? = null
    private var stepCounterService: StepCounterService? = null
    private var heightDifferenceService: HeightDifferenceService? = null

    private val serviceMapper = mapOf(
        DetectedActivity.IN_VEHICLE to ::defineVehicleService,
        DetectedActivity.RUNNING to ::defineRunService,
        DetectedActivity.WALKING to ::defineWalkService
    )

    private lateinit var db: BrockDB
    private lateinit var distanceServiceConnection: ServiceConnection
    private lateinit var heightDifferenceConnection: ServiceConnection
    private lateinit var stepCounterServiceConnection: ServiceConnection

    override fun onCreate() {
        super.onCreate()
        db = BrockDB.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
        } else {
            val activityType = intent.getIntExtra("ACTIVITY_TYPE", 4)
            val arrivalTime = intent.getLongExtra("ARRIVAL_TIME", 0L)
            val exitTime = intent.getLongExtra("EXIT_TIME", 0L)

            when (intent.action) {
                Actions.INSERT.toString() -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        serviceMapper[activityType]?.invoke()
                        insert(activityType, arrivalTime)
                    }
                }

                Actions.UPDATE.toString() -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val job = async {
                            update(activityType, exitTime)
                        }

                        job.await()
                        stopSelf()
                    }
                }

                Actions.RESTART.toString() -> {
                    restartMonitoring()
                    stopSelf()
                }

                Actions.TERMINATE.toString() -> {
                    MyActivityRecognition.removeTask(this)
                    stopSelf()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    enum class Actions {
        INSERT, UPDATE, RESTART, TERMINATE
    }

    private fun defineVehicleService() {
        distanceServiceConnection = MyServiceConnection.createDistanceServiceConnection(
            onConnected = { service ->
                this.distanceService = service
                isDistanceServiceBound = true
            },
            onDisconnected = {
                isDistanceServiceBound = false
            }
        )
    }

    private fun defineRunService() {
        distanceServiceConnection = MyServiceConnection.createDistanceServiceConnection(
            onConnected = { service ->
                this.distanceService = service
                isDistanceServiceBound = true
            },
            onDisconnected = {
                isDistanceServiceBound = false
            }
        )

        heightDifferenceConnection = MyServiceConnection.createHeightDifferenceService(
            onConnected = { service ->
                heightDifferenceService = service
                isHeightDifferenceServiceBound = true
            },
            onDisconnected = {
                isHeightDifferenceServiceBound = false
            }
        )
    }

    private fun defineWalkService() {
        stepCounterServiceConnection = MyServiceConnection.createStepCounterService(
            onConnected = { service ->
                stepCounterService = service
                isStepCounterServiceBound = true
            },
            onDisconnected = {
                isStepCounterServiceBound = false
            }
        )

        heightDifferenceConnection = MyServiceConnection.createHeightDifferenceService(
            onConnected = { service ->
                heightDifferenceService = service
                isHeightDifferenceServiceBound = true
            },
            onDisconnected = {
                isHeightDifferenceServiceBound = false
            }
        )
    }

    private suspend fun insert(activityType: Int, arrivalTime: Long) {
        when (activityType) {
            DetectedActivity.IN_VEHICLE -> {
                val intent = Intent(this, DistanceService::class.java)
                startService(intent)
                bindService(intent, distanceServiceConnection, Context.BIND_AUTO_CREATE)

                db.UsersVehicleActivityDao().insertVehicleActivity(
                    UsersVehicleActivityEntity(
                        username = MyUser.username,
                        timestamp = getInstant(),
                        arrivalTime = arrivalTime,
                        exitTime = 0L,
                        distanceTravelled = 0.0
                    )
                )
            }

            DetectedActivity.RUNNING -> {
                Intent(this, StepCounterService::class.java).also {
                    startService(it)
                    bindService(it, stepCounterServiceConnection, Context.BIND_AUTO_CREATE)
                }

                Intent(this, HeightDifferenceService::class.java).also {
                    startService(it)
                    bindService(it, heightDifferenceConnection, Context.BIND_AUTO_CREATE)
                }

                db.UsersRunActivityDao().insertRunActivity(
                    UsersRunActivityEntity(
                        username = MyUser.username,
                        timestamp = getInstant(),
                        arrivalTime = arrivalTime,
                        exitTime = 0L,
                        distanceDone = 0.0,
                        heightDifference = 0f
                    )
                )
            }

            DetectedActivity.STILL -> {
                db.UsersStillActivityDao().insertStillActivity(
                    UsersStillActivityEntity(
                        username = MyUser.username,
                        timestamp = getInstant(),
                        arrivalTime = arrivalTime,
                        exitTime = 0L,
                    )
                )
            }

            DetectedActivity.WALKING -> {
                Intent(this, StepCounterService::class.java).also {
                    startService(it)
                    bindService(it, stepCounterServiceConnection, Context.BIND_AUTO_CREATE)
                }

                Intent(this, HeightDifferenceService::class.java).also {
                    startService(it)
                    bindService(it, heightDifferenceConnection, Context.BIND_AUTO_CREATE)
                }

                db.UsersWalkActivityDao().insertWalkActivity(
                    UsersWalkActivityEntity(
                        username = MyUser.username,
                        timestamp = getInstant(),
                        arrivalTime = arrivalTime,
                        exitTime = 0L,
                        stepsNumber = 0L,
                        heightDifference = 0f
                    )
                )
            }
        }
    }

    private fun getInstant(): String {
        return DateTimeFormatter
            .ofPattern(ISO_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }

    private suspend fun update(activityType: Int, exitTime: Long) {
        when(activityType) {
            DetectedActivity.IN_VEHICLE -> {
                if (isDistanceServiceBound)  {
                    val distanceTravelled = distanceService?.getDistance() ?: 0.0
                    unbindService(distanceServiceConnection)

                    val lastId = db.UsersVehicleActivityDao().getLastInsertedId()
                    db.UsersVehicleActivityDao().updateLastRecord(
                        lastId,
                        exitTime,
                        distanceTravelled
                    )
                }
            }

            DetectedActivity.RUNNING -> {
                if (isDistanceServiceBound && isHeightDifferenceServiceBound) {
                    val distanceDone = distanceService?.getDistance() ?: 0.0
                    unbindService(distanceServiceConnection)

                    val heightDifference = heightDifferenceService?.getAltitude() ?: 0f
                    unbindService(heightDifferenceConnection)

                    val lastId = db.UsersRunActivityDao().getLastInsertedId()
                    db.UsersRunActivityDao().updateLastRecord(
                        lastId,
                        exitTime,
                        distanceDone,
                        heightDifference
                    )
                }
            }

            DetectedActivity.STILL -> {
                val lastId = db.UsersStillActivityDao().getLastInsertedId()
                db.UsersStillActivityDao().updateLastRecord(
                    lastId,
                    exitTime
                )
            }

            DetectedActivity.WALKING -> {
                if (isStepCounterServiceBound && isHeightDifferenceServiceBound) {
                    val stepsNumber = stepCounterService?.getSteps() ?: 0L
                    unbindService(stepCounterServiceConnection)

                    val heightDifference = heightDifferenceService?.getAltitude() ?: 0f
                    unbindService(heightDifferenceConnection)

                    val lastId = db.UsersRunActivityDao().getLastInsertedId()
                    db.UsersWalkActivityDao().updateLastRecord(
                        lastId,
                        exitTime,
                        stepsNumber,
                        heightDifference
                    )
                }
            }
        }
    }

    private fun restartMonitoring() {
        MyActivityRecognition.removeTask(this)
        MyActivityRecognition.getTask(this)?.run {
            addOnSuccessListener {
                MyActivityRecognition.setStatus(true)
            }
            addOnFailureListener {
                Log.d("PAGE_LOADER_ACTIVITY", "Unsuccessful connection")
            }
        }
    }
}