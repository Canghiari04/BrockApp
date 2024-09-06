package com.example.brockapp.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.brockapp.singleton.MyGeofence
import com.google.android.gms.location.LocationServices

class MapService: Service() {
    private lateinit var geofence: MyGeofence

    override fun onCreate() {
        super.onCreate()
        geofence = MyGeofence.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val geofenceClient = LocationServices.getGeofencingClient(this)

            geofenceClient.removeGeofences(geofence.pendingIntent).run {
                addOnSuccessListener {
                    Log.d("CONNECTIVITY_SERVICE", "Geofence removed.")
                }
            }

            geofence.defineRequest()

            geofenceClient.addGeofences(geofence.request, geofence.pendingIntent).run {
                addOnSuccessListener {
                    Log.d("CONNECTIVITY_SERVICE", "Geofence added.")
                }
            }
        } else {
            Log.e("WTF", "WTF.")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}