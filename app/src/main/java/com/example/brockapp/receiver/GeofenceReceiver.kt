package com.example.brockapp.receiver

import com.example.brockapp.GEOFENCE_INTENT_TYPE

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.service.GeofenceWorker
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == GEOFENCE_INTENT_TYPE) {
            val event = GeofencingEvent.fromIntent(intent)

            if(event != null) {
                if(event.hasError()) {
                    Log.d("GEOFENCING_SERVICE", event.errorCode.toString())
                } else {
                    val geofenceTransition = event.geofenceTransition
                    val geofenceLocation = event.triggeringLocation

                    val (latitude, longitude) = Pair(geofenceLocation?.latitude, geofenceLocation?.longitude)

//                    val serviceIntent = Intent(context, GeofenceService::class.java).apply {
//                        putExtra("TRANSITION", geofenceTransition)
//                        putExtra("LATITUDE", latitude)
//                        putExtra("LONGITUDE", longitude)
//                    }

                    val workRequest = OneTimeWorkRequest.Builder(GeofenceWorker::class.java).build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                }
            } else {
                Log.d("GEOFENCING_SERVICE", "Null event.")
            }
        }
    }
}