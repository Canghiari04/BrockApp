package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.worker.GeofenceWorker
import com.example.brockapp.service.GeofenceService

import android.util.Log
import java.util.Locale
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import android.location.Geocoder
import android.location.Location
import androidx.work.OneTimeWorkRequest
import android.content.BroadcastReceiver
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL

class GeofenceReceiver: BroadcastReceiver() {
    private lateinit var workRequest: OneTimeWorkRequest

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == GEOFENCE_INTENT_TYPE) {
            val event = GeofencingEvent.fromIntent(intent)

            if(event != null) {
                if(event.hasError()) {
                    Log.e("GEOFENCE_RECEIVER", event.errorCode.toString())
                } else {
                    val geofenceLocation = event.triggeringLocation
                    val geofenceTransition = event.geofenceTransition

                    when (geofenceTransition) {
                        GEOFENCE_TRANSITION_DWELL -> {
                            val arrivalTime = System.currentTimeMillis()

                            // Returns the feature name, the principal meaning of the geofence area
                            val nameLocation = getLocationNameByCoordinates(
                                geofenceLocation,
                                context
                            )

                            if (geofenceLocation != null) {
                                context.startService(
                                    Intent(context, GeofenceService::class.java).apply {
                                        putExtra("LOCATION_NAME", nameLocation)
                                        putExtra("LATITUDE", geofenceLocation.latitude)
                                        putExtra("LONGITUDE", geofenceLocation.longitude)
                                        putExtra("ARRIVAL_TIME", arrivalTime)
                                        putExtra("EXIT_TIME", 0)
                                    }
                                )
                            }

                            workRequest = OneTimeWorkRequest.Builder(GeofenceWorker::class.java).build()
                            WorkManager.getInstance(context).enqueue(workRequest)
                        }

                        GEOFENCE_TRANSITION_EXIT -> {
                            val exitTime = System.currentTimeMillis()

                            context.startService(
                                Intent(context, GeofenceService::class.java).apply {
                                    putExtra("LOCATION_NAME", " ")
                                    putExtra("LATITUDE", 0.0)
                                    putExtra("LONGITUDE", 0.0)
                                    putExtra("ARRIVAL_TIME", -1L)
                                    putExtra("EXIT_TIME", exitTime)
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

    private fun getLocationNameByCoordinates(geofenceLocation: Location?, context: Context): String? {
        val latitude = geofenceLocation?.latitude
        val longitude = geofenceLocation?.longitude

        val geocoder = Geocoder(context, Locale.getDefault())
        val address = geocoder.getFromLocation(latitude!!, longitude!!, 1)

        return if (!address.isNullOrEmpty()) {
            address[0].featureName ?: address[0].getAddressLine(0)
        } else {
            null
        }
    }
}