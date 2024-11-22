package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.room.UsersRunActivityEntity
import com.example.brockapp.room.UsersWalkActivityEntity
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.room.UsersStillActivityEntity
import com.example.brockapp.room.UsersVehicleActivityEntity
import com.example.brockapp.singleton.MyActivityRecognition

import android.os.Build
import android.util.Log
import java.time.Instant
import android.os.IBinder
import android.app.Service
import java.time.ZoneOffset
import android.content.Intent
import android.hardware.Sensor
import android.content.Context
import android.location.Location
import kotlinx.coroutines.launch
import android.hardware.SensorEvent
import kotlinx.coroutines.Dispatchers
import android.hardware.SensorManager
import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import java.time.format.DateTimeFormatter
import android.hardware.SensorEventListener
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

class ActivityRecognitionService: Service(), SensorEventListener, NotificationSender {

    private var initialStepCount = 0L
    private var sessionStepsCount = 0L
    private var distance: Double = 0.0
    private var startLocation: Location? = null
    private var stepCounterSensor: Sensor? = null
    private var notificationUtil = NotificationUtil()

    private lateinit var db: BrockDB

    private lateinit var sensorManager: SensorManager
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        db = BrockDB.getInstance(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        fusedLocationClient =  LocationServices.getFusedLocationProviderClient(this)

        setUpLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
        } else {
            when (intent.action) {
                Actions.START.toString() -> {
                    startForeground(
                        ID_ACTIVITY_RECOGNITION_WORKER_NOTIFY,
                        notificationUtil.getNotificationBody(
                            CHANNEL_ID_ACTIVITY_RECOGNITION_WORKER,
                            R.drawable.icon_run,
                            "BrockApp - Activity Recognition Service",
                            "BrockApp is monitoring your activity as long as it's active",
                            this
                        ).build()
                    )
                }

                Actions.INSERT.toString() -> {
                    val type = intent.getIntExtra("ACTIVITY_TYPE", 4)
                    val arrivalTime = intent.getLongExtra("ARRIVAL_TIME", 0L)

                    notificationUtil.updateActivityRecognitionNotification(
                        ID_ACTIVITY_RECOGNITION_WORKER_NOTIFY,
                        type,
                        this
                    )

                    startSensors(type)

                    CoroutineScope(Dispatchers.IO).launch {
                        insert(type, arrivalTime)
                    }
                }

                Actions.UPDATE.toString() -> {
                    val type = intent.getIntExtra("ACTIVITY_TYPE", 4)
                    val exitTime = intent.getLongExtra("EXIT_TIME", 0L)

                    CoroutineScope(Dispatchers.IO).launch {
                        update(type, exitTime)
                    }
                }

                Actions.RESTART.toString() -> {
                    restartMonitoring()
                }

                Actions.STOP.toString() -> {
                    stopSelf()
                }

                Actions.TERMINATE.toString() -> {
                    terminateMonitoring()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun sendNotification(title: String, content: String) {
        notificationUtil.getNotificationBody(
            CHANNEL_ID_STEP_COUNTER_SERVICE,
            R.drawable.icon_run,
            title,
            content,
            this
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (initialStepCount == 0L) {
            initialStepCount = event.values[0].toLong()
        }

        sessionStepsCount = event.values[0].toInt() - initialStepCount
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    enum class Actions {
        START, INSERT, UPDATE, RESTART, STOP, TERMINATE
    }

    private fun setUpLocationUpdates() {
        locationRequest = LocationRequest
            .Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val locations = locationResult.locations

                if (locations.isNotEmpty()) {
                    val newLocation = locations.last()

                    if (startLocation == null) {
                        startLocation = newLocation
                    } else {
                        startLocation?.let {
                            distance += it.distanceTo(newLocation).toDouble()
                        }

                        startLocation = newLocation
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startSensors(type: Int) {
        when (type) {
            DetectedActivity.IN_VEHICLE -> {
                distance = 0.0
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }

            DetectedActivity.RUNNING -> {
                distance = 0.0
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }

            DetectedActivity.WALKING -> {
                if (stepCounterSensor != null) {
                    sessionStepsCount = 0L

                    sensorManager.registerListener(
                        this,
                        stepCounterSensor,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                } else {
                    sendNotification(
                        "BrockApp - Step counter service",
                        "Step counter sensor is not available in this device"
                    )
                }
            }
        }
    }

    private suspend fun insert(type: Int, arrivalTime: Long) {
        when (type) {
            DetectedActivity.IN_VEHICLE -> {
                db.UsersVehicleActivityDao().insertVehicleActivity(
                    UsersVehicleActivityEntity(
                        username = MyUser.username,
                        timestamp = getInstant(),
                        arrivalTime = System.currentTimeMillis(),
                        exitTime = 0L,
                        distanceTravelled = 0.0
                    )
                )
            }

            DetectedActivity.RUNNING -> {
                db.UsersRunActivityDao().insertRunActivity(
                    UsersRunActivityEntity(
                        username = MyUser.username,
                        timestamp = getInstant(),
                        arrivalTime = System.currentTimeMillis(),
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
                        exitTime = 0L
                    )
                )
            }

            DetectedActivity.WALKING -> {
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

    private suspend fun update(type: Int, exitTime: Long) {
        when(type) {
            DetectedActivity.IN_VEHICLE -> {
                db.UsersVehicleActivityDao().updateLastRecord(
                    db.UsersVehicleActivityDao().getLastInsertedId(),
                    System.currentTimeMillis(),
                    distance
                )

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            DetectedActivity.RUNNING -> {
                db.UsersRunActivityDao().updateLastRecord(
                    db.UsersRunActivityDao().getLastInsertedId(),
                    System.currentTimeMillis(),
                    distance,
                    0f
                )

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            DetectedActivity.STILL -> {
                db.UsersStillActivityDao().updateLastRecord(
                    db.UsersStillActivityDao().getLastInsertedId(),
                    exitTime
                )
            }

            DetectedActivity.WALKING -> {
                db.UsersWalkActivityDao().updateLastRecord(
                    db.UsersWalkActivityDao().getLastInsertedId(),
                    exitTime,
                    sessionStepsCount,
                    0f
                )

                sensorManager.unregisterListener(this)
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

    private fun terminateMonitoring() {
        MyActivityRecognition.removeTask(this)
        stopSelf()
    }
}