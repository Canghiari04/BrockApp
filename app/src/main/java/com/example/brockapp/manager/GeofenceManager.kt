package com.example.brockapp.manager

import com.example.brockapp.data.Locality
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.database.GeofenceAreaEntry
import com.example.brockapp.REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

class GeofenceManager(private val context: Context, private val areas: List<GeofenceAreaEntry>?) {
    companion object {
        const val r = 1000
        const val day = 24 * 60 * 60 * 1000L
    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceService::class.java)

        return PendingIntent.getService(
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
        val entries = getEntries()
        val listGeofence: MutableList<Geofence> = mutableListOf()

        for(entry in entries) {
            listGeofence.add(
                Geofence.Builder()
                    .setRequestId(entry.id)
                    .setCircularRegion(entry.longitude, entry.latitude, r.toFloat())
                    .setExpirationDuration(day)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            )
        }

        return listGeofence
    }

    private fun getEntries(): List<Locality> {
        val listLocalities: MutableList<Locality> = mutableListOf()

        if (areas != null) {
            for (area in areas) {
                listLocalities.add(
                    Locality(area.id.toString(), area.longitude, area.latitude)
                )
            }
        }

        return listLocalities
    }
}