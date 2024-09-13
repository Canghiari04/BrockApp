package com.example.brockapp

import com.example.brockapp.singleton.MyGeofence

import android.app.Application

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MyGeofence.initPendingIntent(this)
    }
}