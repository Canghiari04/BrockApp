package com.example.brockapp.service

import com.example.brockapp.GEOFENCE_INTENT_TYPE

import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingEvent
import java.util.Locale

class GeofenceService: Service() {
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == GEOFENCE_INTENT_TYPE) {
                    val event = GeofencingEvent.fromIntent(intent)

                    if(event != null) {
                        if(event.hasError()) {
                            Log.d("GEOFENCE_BROADCAST", event.errorCode.toString())
                        } else {
                            val geofenceTransition = event.geofenceTransition
                            val geofenceLocation = event.triggeringLocation
                            val geofences = event.triggeringGeofences

                            when {
                                geofenceTransition == GEOFENCE_TRANSITION_ENTER -> {
                                    handleGeofencingTransition(geofenceTransition, geofenceLocation, geofences)
                                }
                                geofenceTransition == GEOFENCE_TRANSITION_DWELL -> {
                                    handleGeofencingTransition(geofenceTransition, geofenceLocation, geofences)
                                }
                                else -> {
                                    Log.d("GEOFENCE_BROADCAST", "Geofence non necessario.")
                                }
                            }
                        }
                    } else {
                        Log.d("GEOFENCE_BROADCAST", "Evento null.")
                    }
                }
            }

        }

        registerReceiver(receiver, IntentFilter(GEOFENCE_INTENT_TYPE))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun handleGeofencingTransition(geofenceTransition: Int, geofenceLocation: Location?, geofences: List<Geofence>?) {
        // DOVREI INVIARE UNA NOTIFICA
        Log.d("GEOFENCE_BROADCAST", "I'm in.")
    }
}