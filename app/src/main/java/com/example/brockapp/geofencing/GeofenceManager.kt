package com.example.brockapp.geofencing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.brockapp.REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

class GeofenceManager(private val context: Context?) {

    companion object {
        const val longitude = 44.4827194
        const val latitude = 11.3498368
        const val r = 1000
        const val day = 24 * 60 * 60 * 1000L
    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getRequest(): GeofencingRequest {
        val list = getGeofences()

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }

    private fun getGeofences(): List<Geofence> {
        val listGeofence: MutableList<Geofence> = mutableListOf()

        listGeofence.add(
            Geofence.Builder()
                .setRequestId("1")
                .setCircularRegion(longitude, latitude, r.toFloat())
                .setExpirationDuration(day)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())

        return listGeofence
    }
}