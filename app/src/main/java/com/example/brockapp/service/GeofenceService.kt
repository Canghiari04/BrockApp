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

class GeofenceService : Service() {
    private lateinit var receiver: BroadcastReceiver
    private lateinit var utilNotification: NotificationUtil

    override fun onCreate() {
        super.onCreate()

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("GEOFENCE_BROADCAST", "Fuori dalla condizione.")

                if(intent.action == GEOFENCE_INTENT_TYPE) {
                    val event = GeofencingEvent.fromIntent(intent)

                    Log.d("GEOFENCE_BROADCAST", "Sono dentro al receiver.")

                    if(event != null) {
                        if(event.hasError()) {
                            Log.d("GEOFENCE_BROADCAST", event.errorCode.toString())
                        } else {
                            val geofenceTransition = event.geofenceTransition
                            val geofenceLocation = event.triggeringLocation

                            when (geofenceTransition) {
                                GEOFENCE_TRANSITION_ENTER -> {
                                    val location = getLocation(geofenceLocation!!)
                                    Log.d("GEOFENCE_BROADCAST", "Invio la notifica dopo ENTER.")
                                    sendGeofenceNotify(location)
                                }
                                GEOFENCE_TRANSITION_DWELL -> {
                                    val location = getLocation(geofenceLocation!!)
                                    Log.d("GEOFENCE_BROADCAST", "Invio la notifica dopo DWELL.")
                                    sendGeofenceNotify(location)
                                }
                                else -> {
                                    Log.d("GEOFENCE_BROADCAST", "Transition non riconosciuta.")
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun getLocation(item: Location): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val coordinates = Pair(item.latitude, item.longitude)
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