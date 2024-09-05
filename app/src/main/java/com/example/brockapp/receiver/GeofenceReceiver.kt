package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.worker.GeofenceWorker

import android.util.Log
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequest
import android.content.BroadcastReceiver
import com.google.android.gms.location.GeofencingEvent
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
                    val geofenceTransition = event.geofenceTransition

                    when (geofenceTransition) {
                        GEOFENCE_TRANSITION_DWELL -> {
                            workRequest = OneTimeWorkRequest.Builder(GeofenceWorker::class.java).build()
                            WorkManager.getInstance(context).enqueue(workRequest)
                        }

                        else -> {
                            Log.e("GEOFENCE_RECEIVER", "Transition not recognize.")
                        }
                    }
                }
            } else {
                Log.d("GEOFENCE_RECEIVER", "Null event.")
            }
        }
    }
}