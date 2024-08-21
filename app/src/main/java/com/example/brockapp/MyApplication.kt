package com.example.brockapp

import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.receiver.ConnectivityReceiver

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat

class MyApplication: Application() {
    private lateinit var db: BrockDB
    private lateinit var viewModel: GeofenceViewModel
    private lateinit var receiverConnectivity: ConnectivityReceiver

    override fun onCreate() {
        super.onCreate()

        db = BrockDB.getInstance(this)

        viewModel = GeofenceViewModel(db)
        viewModel.insertStaticGeofenceAreas()

        receiverConnectivity = ConnectivityReceiver()
        ContextCompat.registerReceiver(this, receiverConnectivity, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(receiverConnectivity)
    }
}