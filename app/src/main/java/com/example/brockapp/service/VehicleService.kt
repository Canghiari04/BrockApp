package com.example.brockapp.service

import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.hardware.SensorManager

class VehicleSensor: Service() {
    private lateinit var sensorManager: SensorManager

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startMonitoring() {

    }

    private fun stopMonitoring() {

    }
}