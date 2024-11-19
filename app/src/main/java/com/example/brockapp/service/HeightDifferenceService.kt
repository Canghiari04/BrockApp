package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil

import android.os.Binder
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener

class HeightDifferenceService: Service(), SensorEventListener {

    private var sensor: Sensor? = null
    private var binder = LocalBinder()
    private var previousAltitude: Float? = null
    private var notificationUtil = NotificationUtil()
    private var totalNegativeHeightDifference: Float = 0f
    private var totalPositiveHeightDifference: Float = 0f

    private lateinit var sensorManager: SensorManager

    inner class LocalBinder: Binder() {
        fun getService(): HeightDifferenceService = this@HeightDifferenceService
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

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

    override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
                val currentAltitude = convertPressureToAltitude(event.values[0])

                previousAltitude?.let {
                    val difference = currentAltitude - it

                    if (difference > 0) {
                        totalPositiveHeightDifference += difference
                    } else {
                        totalNegativeHeightDifference += difference
                    }
                }

                previousAltitude = currentAltitude
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    fun resetAltitude() {
        totalNegativeHeightDifference = 0f
        totalPositiveHeightDifference = 0f
    }

    fun getAltitude(): Float {
        return (totalPositiveHeightDifference - totalNegativeHeightDifference)
    }

    private fun start() {
        if (sensor != null) {
            startForeground(
                ID_HEIGHT_DIFFERENCE_SERVICE_NOTIFY,
                notificationUtil.getNotificationBody(
                    CHANNEL_ID_HEIGHT_DIFFERENCE_SERVICE,
                    R.drawable.icon_run,
                    "Brock App - Height difference monitoring",
                    "Brock App is tracking the height difference in background",
                    this
                ).build()
            )
        }
    }

    private fun startMonitoring() {
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        stopSelf()
    }

    private fun convertPressureToAltitude(sessionPressure: Float): Float {
        return SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, sessionPressure)
    }
}