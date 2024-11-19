package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.worker.GeofenceProcessingWorker

import android.util.Log
import androidx.work.Data
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import android.content.BroadcastReceiver
import androidx.work.OneTimeWorkRequestBuilder
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == GEOFENCE_INTENT_TYPE) {
            val event = GeofencingEvent.fromIntent(intent)

            if (event != null) {
                if (event.hasError()) {
                    Log.e("GEOFENCE_RECEIVER", event.errorCode.toString())
                } else {
                    val transition = event.geofenceTransition
                    val location = event.triggeringLocation

                    context?.let {
                        location?.let {
                            val inputData = Data.Builder()
                                .putInt("TRANSITION", transition)
                                .putDouble("LATITUDE", it.latitude)
                                .putDouble("LONGITUDE", it.longitude)
                                .build()

                            val request = OneTimeWorkRequestBuilder<GeofenceProcessingWorker>()
                                .setInputData(inputData)
                                .build()

                            WorkManager.getInstance(context).enqueue(request)
                        }
                    }
                }
            } else {
                Log.d("GEOFENCE_RECEIVER", "Null event")
            }
        }
    }
}