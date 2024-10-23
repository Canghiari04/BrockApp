package com.example.brockapp.service

import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityRecognitionWorker

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
    private var sessionStepCount = 0L
    private var sensor: Sensor? = null
    private var binder = LocalBinder()

    private lateinit var sensorManager: SensorManager

    inner class LocalBinder: Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopMonitoring()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (initialStepCount < 0) {
            initialStepCount = event.values[0].toLong()
        }

        sessionStepCount = event.values[0].toInt() - initialStepCount

        if (sessionStepCount > 20) {
            sendNotification(
                "BrockApp - Keep walking",
                "You're going great! Continue walking for few minutes and take a deep breathe"
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }

    override fun sendNotification(title: String, content: String) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("text", content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ActivityRecognitionWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    fun getSteps(): Long {
        return sessionStepCount
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
    }

    private fun startMonitoring() {
        if (sensor == null) {
            sendNotification(
                "BrockApp - Step Counter Sensor",
                "The step counter sensor is not present in this device"
            )
        } else {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }
}