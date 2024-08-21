package com.example.brockapp

import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.GeofenceViewModel

import android.app.Application

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

//        val db = BrockDB.getInstance(this)
//
//        val viewModel = GeofenceViewModel(db)
//        viewModel.insertStaticGeofenceAreas()
    }
}