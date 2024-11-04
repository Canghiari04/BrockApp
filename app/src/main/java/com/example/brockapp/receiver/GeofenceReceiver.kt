package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.service.GeofenceService

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL

class GeofenceReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == GEOFENCE_INTENT_TYPE) {
            val event = GeofencingEvent.fromIntent(intent)

            if (event != null) {
                if (event.hasError()) {
                    Log.e("GEOFENCE_RECEIVER", event.errorCode.toString())
                } else {
                    val geofenceTransition = event.geofenceTransition

                    when (geofenceTransition) {
                        GEOFENCE_TRANSITION_DWELL -> {
                            val geofenceLocation = event.triggeringLocation

                            if (geofenceLocation != null) {
                                val arrivalTime = System.currentTimeMillis()

                                context.startService(
                                    Intent(context, GeofenceService::class.java).also {
                                        it.action = GeofenceService.Actions.INSERT.toString()
                                        it.putExtra("LATITUDE", geofenceLocation.latitude)
                                        it.putExtra("LONGITUDE", geofenceLocation.longitude)
                                        it.putExtra("ARRIVAL_TIME", arrivalTime)
                                        it.putExtra("EXIT_TIME", 0L)
                                    }
                                )
                            }
                        }

                        GEOFENCE_TRANSITION_EXIT -> {
                            val exitTime = System.currentTimeMillis()

                            context.startService(
                                Intent(context, GeofenceService::class.java).also {
                                    it.action = GeofenceService.Actions.UPDATE.toString()
                                    it.putExtra("EXIT_TIME", exitTime)
                                }
                            )
                        }

                        else -> {
                            Log.e("GEOFENCE_RECEIVER", "Transition not recognize")
                        }
                    }
                }
            } else {
                Log.d("GEOFENCE_RECEIVER", "Null event")
            }
        }
    }
}