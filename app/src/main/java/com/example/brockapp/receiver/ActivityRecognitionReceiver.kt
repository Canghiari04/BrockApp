package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.worker.ActivityPreprocessingWorker

import androidx.work.Data
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import android.content.BroadcastReceiver
import androidx.work.OneTimeWorkRequestBuilder
import com.google.android.gms.location.ActivityTransitionResult

class ActivityRecognitionReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTIVITY_RECOGNITION_INTENT_TYPE) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            val events = result.transitionEvents

            for (event in events) {
                val type = event.activityType
                val transition = event.transitionType

                context?.let {
                    val inputData = Data.Builder()
                        .putInt("TYPE", type)
                        .putInt("TRANSITION", transition)
                        .build()

                    val request = OneTimeWorkRequestBuilder<ActivityPreprocessingWorker>()
                        .setInputData(inputData)
                        .build()

                    WorkManager.getInstance(context).enqueue(request)
                }
            }
        }
    }
}