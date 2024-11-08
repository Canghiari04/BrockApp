package com.example.brockapp.singleton

import com.example.brockapp.*
import com.example.brockapp.data.Locality
import com.example.brockapp.room.GeofenceAreasEntity
import com.example.brockapp.receiver.GeofenceReceiver

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

class MyGeofence private constructor() {
    companion object {
        private var radius = 0
        private var duration = 86400000L

        @Volatile
        private var status = false

        @Volatile
        private var pendingIntent: PendingIntent? = null

        @Volatile
        private var areas = mutableListOf<GeofenceAreasEntity>()

        fun getStatus(): Boolean {
            synchronized(this) {
                return status
            }
        }

        fun setStatus(item: Boolean) {
            synchronized(this) {
                status = item
            }
        }

        fun getPendingIntent(context: Context): PendingIntent {
            synchronized(this) {
                if (pendingIntent == null) {
                    pendingIntent = createPendingIntent(context)
                }
            }

            return pendingIntent!!
        }

        fun getRequest(): GeofencingRequest {
            synchronized(this) {
                return createRequest()
            }
        }

        fun defineRadius(context: Context) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            val activeNetwork = connectivityManager.getNetworkCapabilities(network)

            if (activeNetwork != null) {
                radius = when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        150
                    }

                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        200
                    }

                    else -> {
                        250
                    }
                }
            } else {
                radius = 250
            }
        }

        fun defineAreas(items: List<GeofenceAreasEntity>) {
            synchronized(this) {
                areas = mutableListOf()
                areas = items.toMutableList()
            }
        }

        private fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, GeofenceReceiver::class.java).apply {
                action = GEOFENCE_INTENT_TYPE
            }

            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }

        private fun createRequest(): GeofencingRequest {
            val list = createEntries()

            return GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                addGeofences(list)
            }.build()
        }

        private fun createEntries(): List<Geofence> {
            val listLocalities: MutableList<Locality> = mutableListOf()

            for (area in areas) {
                listLocalities.add(
                    Locality(area.id.toString(), area.longitude, area.latitude)
                )
            }

            val listGeofence = mutableListOf<Geofence>()

            for(locality in listLocalities) {
                listGeofence.add(
                    Geofence.Builder()
                        .setRequestId(locality.id)
                        .setCircularRegion(locality.latitude, locality.longitude, radius.toFloat())
                        .setExpirationDuration(duration)
                        .setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_DWELL
                            or
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay(5000)
                        .build()
                )
            }

            return listGeofence
        }
    }
}