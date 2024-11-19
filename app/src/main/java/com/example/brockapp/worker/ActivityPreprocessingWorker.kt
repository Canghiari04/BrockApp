package com.example.brockapp.worker

import com.example.brockapp.service.ActivityRecognitionService

import androidx.work.Worker
import android.content.Intent
import android.content.Context
import androidx.work.WorkerParameters

class ActivityPreprocessingWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val type = inputData.getInt("TYPE", 4)
        val transition = inputData.getInt("TRANSITION", -1)

        val serviceIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            val items = if (transition == 0) {
                Pair(
                    ActivityRecognitionService.Actions.INSERT.toString(),
                    "ARRIVAL_TIME"
                )
            } else {
                Pair(
                    ActivityRecognitionService.Actions.UPDATE.toString(),
                    "EXIT_TIME"
                )
            }

            action = items.first
            putExtra("ACTIVITY_TYPE", type)
            putExtra(items.second, System.currentTimeMillis())
        }

        context.startService(serviceIntent)

        return Result.success()
    }
}