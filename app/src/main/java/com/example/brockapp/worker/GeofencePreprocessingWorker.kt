package com.example.brockapp.worker

import com.example.brockapp.service.GeofenceService

import androidx.work.Worker
import android.content.Context
import android.content.Intent
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL

class GeofencePreprocessingWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        val transition = inputData.getInt("TRANSITION", -1)
        val latitude = inputData.getDouble("LATITUDE", 0.0)
        val longitude = inputData.getDouble("LONGITUDE", 0.0)
        val time = System.currentTimeMillis()

        when (transition) {
            GEOFENCE_TRANSITION_DWELL -> {
                context.startService(
                    Intent(context, GeofenceService::class.java).apply {
                        action = GeofenceService.Actions.INSERT.toString()
                        putExtra("LATITUDE", latitude)
                        putExtra("LONGITUDE", longitude)
                        putExtra("ARRIVAL_TIME", time)
                        putExtra("EXIT_TIME", 0L)
                    }
                )
            }

            GEOFENCE_TRANSITION_EXIT -> {
                context.startService(
                    Intent(context, GeofenceService::class.java).apply {
                        action = GeofenceService.Actions.UPDATE.toString()
                        putExtra("EXIT_TIME", time)
                    }
                )
            }

            else -> {
                Result.failure()
            }
        }

        return Result.success()
    }
}