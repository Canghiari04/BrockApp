package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.singleton.MyNetwork
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.worker.ConnectivityWorker
import com.example.brockapp.interfaces.NetworkAvailableImpl

import android.Manifest
import android.util.Log
import androidx.work.Data
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.work.OneTimeWorkRequestBuilder
import com.google.android.gms.location.LocationServices

class ConnectivityService: Service() {
    private val networkUtil = NetworkAvailableImpl()

    private lateinit var geofence: MyGeofence
    private lateinit var util: NotificationUtil

    override fun onCreate() {
        super.onCreate()

        util = NotificationUtil()
        geofence = MyGeofence.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val (significantChange, typeNetwork) = handleSignificantConnectivityChange(this)

        sendNotification()

        if (significantChange) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val geofenceClient = LocationServices.getGeofencingClient(this)

                geofenceClient.removeGeofences(geofence.pendingIntent).run {
                    addOnSuccessListener {
                        Log.d("CONNECTIVITY_SERVICE", "Geofence removed")
                    }
                    addOnFailureListener {
                        Log.e("CONNECTIVITY_SERVICE", "Geofence not removed")
                    }
                }

                geofence.typeNetwork = typeNetwork
                geofence.defineRadius(this)
                geofence.defineRequest()

                geofenceClient.addGeofences(geofence.request, geofence.pendingIntent).run {
                    addOnSuccessListener {
                        Log.d("CONNECTIVITY_SERVICE", "Geofence added")
                    }
                    addOnFailureListener {
                        Log.e("CONNECTIVITY_SERVICE", "Geofence not added")
                    }
                }
            } else {
                Log.wtf("CONNECTIVITY_SERVICE", "Permission denied")
            }
        } else {
            Log.d("CONNECTIVITY_SERVICE", "Insignificant change")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendNotification() {
        val isConnected = networkUtil.isInternetActive(this)

        when (isConnected) {
            true -> {
                if(isConnected != MyNetwork.isConnected) {
                    MyNetwork.isConnected = true

                    val inputData = Data.Builder()
                        .putString("type", "true")
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<ConnectivityWorker>()
                        .setInputData(inputData)
                        .build()
                    WorkManager.getInstance(this).enqueue(workRequest)
                }
            }

            false -> {
                MyNetwork.isConnected = false

                val inputData = Data.Builder()
                    .putString("type", "false")
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ConnectivityWorker>()
                    .setInputData(inputData)
                    .build()
                WorkManager.getInstance(this).enqueue(workRequest)
            }
        }
    }

    private fun handleSignificantConnectivityChange(context: Context): Pair<Boolean, String> {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)

        val currentTypeNetwork = when {
            activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                WI_FI_TYPE_CONNECTION
            }

            activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                CELLULAR_TYPE_CONNECTION
            }

            else -> {
                NO_CONNECTION_TYPE_CONNECTION
            }
        }

        return Pair(currentTypeNetwork != geofence.typeNetwork, currentTypeNetwork)
    }
}