package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender

import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationManager

class ActivityRecognitionWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), NotificationSender {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()

        val title = inputData.getString("TITLE") ?: " "
        val content = inputData.getString("CONTENT") ?: " "

        sendNotification(
            title,
            content
        )

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getNotificationBody(
            CHANNEL_ID_ACTIVITY_RECOGNITION_WORKER,
            title,
            content,
            context
        )

        manager.notify(ID_ACTIVITY_RECOGNITION_WORKER_NOTIFY, notification.build())
    }
}