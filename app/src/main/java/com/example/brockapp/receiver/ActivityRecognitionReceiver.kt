package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.worker.ActivityPreprocessingWorker

import androidx.work.Data
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import androidx.work.OneTimeWorkRequestBuilder
import com.example.brockapp.util.NotificationUtil
import com.google.android.gms.location.ActivityTransitionResult

class ActivityRecognitionReceiver: BroadcastReceiver() {

    private var notificationUtil = NotificationUtil()

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

                    ///
                    val notification = notificationUtil.getNotificationBody(
                        CHANNEL_ID_MEMO_WORKER,
                        R.drawable.baseline_directions_run_24,
                        "BrockApp - Receiver",
                        "Type ${type}, transition ${transition}",
                        context
                    )

                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(ID_MEMO_WORKER_NOTIFY, notification.build())
                    ///

                    WorkManager.getInstance(context).enqueue(request)
                }
            }
        }
    }
}