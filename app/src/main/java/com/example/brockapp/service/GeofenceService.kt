package com.example.brockapp.service

import com.example.brockapp.GEOFENCE_INTENT_TYPE

import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingEvent

class GeofenceService: Service() {
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == GEOFENCE_INTENT_TYPE) {
                    val event = GeofencingEvent.fromIntent(intent)

                    if(event!!.hasError()) {
                        Log.d("BROADCAST_RECEIVER_GEOFENCING", event.errorCode.toString())
                        return
                    }

                    val transition = event.geofenceTransition

                    // Indipendentemente dalla tipologia di evento a cui sono interessato, vado a gestire
                    // il tutto notificando l'utente.
                    when {
                        transition == GEOFENCE_TRANSITION_ENTER -> {
                            val trigger = event.triggeringGeofences
                        }
                        transition == GEOFENCE_TRANSITION_EXIT -> {
                            val trigger = event.triggeringGeofences
                        }
                        transition == GEOFENCE_TRANSITION_DWELL -> {
                            val trigger = event.triggeringGeofences
                        }
                        else -> {
                            Log.d("WTF", "WTF")
                        }
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
}