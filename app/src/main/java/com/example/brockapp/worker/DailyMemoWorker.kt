package com.example.brockapp.worker

import android.app.NotificationManager
import com.example.brockapp.interfaces.NotificationSender

import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import com.example.brockapp.ID_SYNC_DATA
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.CHANNEL_ID_SYNC_DATA_NOTIFY

class DailyMemoWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), NotificationSender {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()

        sendNotification(
            "BrockApp - Daily memo retrieved",
            "Check your calendar for the current date, there are some memos written by you"
        )

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getNotificationBody(
            CHANNEL_ID_SYNC_DATA_NOTIFY,
            title,
            content,
            context
        )

        manager.notify(ID_SYNC_DATA, notification.build())
    }
}