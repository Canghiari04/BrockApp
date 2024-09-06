package com.example.brockapp.singleton

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brockapp.CELLULAR_TYPE_CONNECTION
import com.example.brockapp.GEOFENCE_INTENT_TYPE
import com.example.brockapp.NO_CONNECTION_TYPE_CONNECTION
import com.example.brockapp.REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER
import com.example.brockapp.WI_FI_TYPE_CONNECTION
import com.example.brockapp.data.Locality
import com.example.brockapp.database.GeofenceAreaEntry
import com.example.brockapp.receiver.GeofenceReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

object MyGeofence {
    private var radius = 0
    private val instance = MyGeofence
    private var duration = 86400000L

    lateinit var request: GeofencingRequest
    lateinit var pendingIntent: PendingIntent

    var geofences: List<GeofenceAreaEntry> = mutableListOf()
    var typeNetwork: String ?= null

    fun getInstance(): MyGeofence {
        return instance
    }

    fun initPendingIntent(context: Context) {
        defineRadius(context)
        definePendingIntent(context)
    }

    fun initAreas(areas: List<GeofenceAreaEntry>) {
        this.geofences = areas
        defineRequest()
    }

    fun defineRadius(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)

        if (activeNetwork != null) {
            when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    typeNetwork = WI_FI_TYPE_CONNECTION
                    radius = 150
                }

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    typeNetwork = CELLULAR_TYPE_CONNECTION
                    radius = 200
                }

                else -> {
                    typeNetwork = NO_CONNECTION_TYPE_CONNECTION
                    radius = 250
                }
            }
        } else {
            typeNetwork = NO_CONNECTION_TYPE_CONNECTION
            radius = 250
        }
    }

    private fun definePendingIntent(context: Context) {
        val intent = Intent(context, GeofenceReceiver::class.java).apply {
            action = GEOFENCE_INTENT_TYPE
        }

        pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun defineRequest() {
        val list = getAreas()

        request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(list)
        }.build()
    }

    private fun getAreas(): List<Geofence> {
        val entries = getEntries()
        val listGeofence: MutableList<Geofence> = mutableListOf()

        for(entry in entries) {
            listGeofence.add(
                Geofence.Builder()
                    .setRequestId(entry.id)
                    .setCircularRegion(entry.latitude, entry.longitude, radius.toFloat())
                    .setExpirationDuration(duration)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(5000)
                    .build()
            )
        }

        return listGeofence
    }

    private fun getEntries(): List<Locality> {
        val listLocalities: MutableList<Locality> = mutableListOf()

        for (geofence in geofences) {
            listLocalities.add(
                Locality(geofence.id.toString(), geofence.longitude, geofence.latitude)
            )
        }

        return listLocalities
    }
}