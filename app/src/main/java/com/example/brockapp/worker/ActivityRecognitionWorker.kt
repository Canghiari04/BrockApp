package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.app.NotificationManager

class ActivityRecognitionWorker(private val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override suspend fun doWork(): Result {
        util = NotificationUtil()

        val title = inputData.getString("title")
        val text = inputData.getString("text")
        sendActivityNotification(title, text)

        return Result.success()
    }

    private fun sendActivityNotification(title: String?, text: String?) {
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getActivityRecognitionNotification(
            CHANNEL_ID_ACTIVITY_NOTIFY,
            title,
            text,
            context
        )

        manager.notify(ID_ACTIVITY_NOTIFY, notification.build())
    }
}