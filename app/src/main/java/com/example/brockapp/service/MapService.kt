package com.example.brockapp.service

import com.example.brockapp.singleton.MyGeofence

import android.Manifest
import android.util.Log
import android.os.IBinder
import android.app.Service
import android.content.Intent
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices

class MapService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val geofenceClient = LocationServices.getGeofencingClient(this)

            geofenceClient.removeGeofences(MyGeofence.pendingIntent).run {
                addOnSuccessListener {
                    Log.d("CONNECTIVITY_SERVICE", "Geofence removed.")
                }
            }

            MyGeofence.defineRequest()

            geofenceClient.addGeofences(MyGeofence.request, MyGeofence.pendingIntent).run {
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