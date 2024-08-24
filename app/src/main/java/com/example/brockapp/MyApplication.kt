package com.example.brockapp

import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyGeofence

import android.app.Application

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        BrockDB.getInstance(this)

        val geofence = MyGeofence.getInstance()
        geofence.initPendingIntent(this)
    }
}