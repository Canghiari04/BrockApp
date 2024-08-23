package com.example.brockapp

import android.app.Application
import com.example.brockapp.database.BrockDB

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val db = BrockDB.getInstance(this)

        // Qua creare tutti i canali di notifica.
    }
}