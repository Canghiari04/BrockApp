package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.app.NotificationManager

class ActivityRecognitionWorker(private val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams), NotificationSender {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override suspend fun doWork(): Result {
        util = NotificationUtil()

        val title = inputData.getString("title")!!
        val text = inputData.getString("text")!!
        sendNotification(title, text)

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getNotificationBody(
            CHANNEL_ID_ACTIVITY_NOTIFY,
            title,
            content,
            context
        )

        manager.notify(ID_ACTIVITY_NOTIFY, notification.build())
    }
}