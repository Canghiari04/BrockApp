package com.example.brockapp

import android.app.Application
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyGeofence

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val db = BrockDB.getInstance(this)

        val geofence = MyGeofence.getInstance()
        geofence.init(this)


        // Qua creare tutti i canali di notifica.
    }
}