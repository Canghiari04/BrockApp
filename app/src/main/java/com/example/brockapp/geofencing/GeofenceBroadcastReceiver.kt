package com.example.brockapp.geofencing

import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import android.util.Log
import com.example.brockapp.GEOFENCE_INTENT_TYPE
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GeofenceTransition
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == GEOFENCE_INTENT_TYPE) {
            val event = GeofencingEvent.fromIntent(intent)
            val transition = event?.geofenceTransition

            when {
                transition == GEOFENCE_TRANSITION_ENTER -> {
                    val trigger = event.triggeringGeofences
                }
                else -> {
                    Log.d("WTF", "WTF")
                }
            }
        }
    }
}