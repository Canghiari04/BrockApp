package com.example.brockapp.service

import com.example.brockapp.GEOFENCE_INTENT_TYPE
import com.example.brockapp.util.NotificationUtil

import android.util.Log
import java.util.Locale
import android.os.IBinder
import java.io.IOException
import android.app.Service
import android.content.Intent
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.content.IntentFilter
import android.content.BroadcastReceiver
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER

class GeofenceService: Service() {
    private lateinit var utilNotification: NotificationUtil

    override fun onCreate() {
        super.onCreate()
        utilNotification = NotificationUtil()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            val geofenceTransition = intent.getIntExtra("TRANSITION", 0)
            val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
            val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

            when (geofenceTransition) {
                GEOFENCE_TRANSITION_ENTER -> {
                    val location = getLocation(latitude, longitude)
                    Log.d("GEOFENCING_SERVICE", "Send notify after ENTERING in a fence.")
                    sendGeofenceNotify(location)
                }

                GEOFENCE_TRANSITION_DWELL -> {
                    val location = getLocation(latitude, longitude)
                    Log.d("GEOFENCING_SERVICE", "Send notify after DWELLING in a fence..")
                    sendGeofenceNotify(location)
                }

                else -> {
                    Log.d("GEOFENCING_SERVICE", "Fence not recognize.")
                }
            }
        } else {
            Log.d("GEOFENCING_SERVICE", "Null intent.")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val coordinates = Pair(latitude, longitude)
        var location = ""

        try {
            val addresses = geocoder.getFromLocation(coordinates.first, coordinates.second, 1)

            if(!addresses.isNullOrEmpty()) {
                location = addresses[0].getAddressLine(0)
            }

        } catch (e: IOException) {
            Log.d("Exception", e.toString())
        }

        return location
    }

    private fun sendGeofenceNotify(item: String) {
        val intent = utilNotification.getGeofenceIntent(item)
        sendBroadcast(intent)
    }
}