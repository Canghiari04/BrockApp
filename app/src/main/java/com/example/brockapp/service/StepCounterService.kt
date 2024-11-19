package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityNotifierWorker

import android.os.Binder
import androidx.work.Data
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.content.Context
import androidx.work.WorkManager
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import androidx.work.OneTimeWorkRequestBuilder

class StepCounterService: Service(), SensorEventListener, NotificationSender {

    private var initialStepCount = 0L
    private var sessionStepsCount = 0L
    private var sensor: Sensor? = null
    private var binder = LocalBinder()
    private var notificationUtil = NotificationUtil()

    private lateinit var sensorManager: SensorManager

    inner class LocalBinder: Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        start()
    }

    override fun onBind(intent: Intent?): IBinder {
        startMonitoring()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopMonitoring()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (initialStepCount == 0L) {
            initialStepCount = event.values[0].toLong()
        }

        sessionStepsCount = event.values[0].toInt() - initialStepCount
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun sendNotification(title: String, content: String) {
        val inputData = Data.Builder()
            .putString("TITLE", title)
            .putString("CONTENT", content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ActivityNotifierWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    fun resetStep() {
        sessionStepsCount = 0L
    }

    fun getSteps(): Long {
        return sessionStepsCount
    }

    private fun start() {
        if (sensor != null) {
            startForeground(
                ID_STEP_COUNTER_SERVICE_NOTIFY,
                notificationUtil.getNotificationBody(
                    CHANNEL_ID_STEP_COUNTER_SERVICE,
                    R.drawable.baseline_directions_run_24,
                    "Brock App - Pedometer monitoring",
                    "Brock App is tracking your step count in background",
                    this
                ).build()
            )
        }
    }

    private fun startMonitoring() {
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        stopSelf()
    }
}