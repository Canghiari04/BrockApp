package com.example.brockapp.singleton

import com.example.brockapp.data.Locality
import com.example.brockapp.GEOFENCE_INTENT_TYPE
import com.example.brockapp.WI_FI_TYPE_CONNECTION
import com.example.brockapp.CELLULAR_TYPE_CONNECTION
import com.example.brockapp.database.GeofenceAreaEntry
import com.example.brockapp.NO_CONNECTION_TYPE_CONNECTION
import com.example.brockapp.REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.location.Geofence
import com.example.brockapp.receiver.GeofenceReceiver
import com.google.android.gms.location.GeofencingRequest

object MyGeofence {
    private lateinit var geofences: List<GeofenceAreaEntry>

    private var duration = 86400000L
    private val instance = MyGeofence

    lateinit var request: GeofencingRequest
    lateinit var pendingIntent: PendingIntent

    private var radius = 0

    var typeNetwork: String ?= null

    fun init(context: Context, geofences: List<GeofenceAreaEntry>) {
        this.geofences = geofences
        defineRadius(context)
        defineRequest()
        definePendingIntent(context)
    }

    fun getInstance(): MyGeofence {
        return instance
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
                    radius = 175
                }

                else -> {
                    typeNetwork = NO_CONNECTION_TYPE_CONNECTION
                    radius = 200
                }
            }
        } else {
            radius = 200
        }
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
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
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
}