package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender

import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ConnectivityWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), NotificationSender {
    private var type: Boolean? = null

    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()

        type = inputData.getString("type")?.toBoolean()
        when (type) {
            true -> {
                sendNotification(
                    "BrockApp - You are online again",
                    "The disabled features are now active. Resume using all the functionalities to monitor your progress"
                )
            }

            else -> {
                sendNotification(
                    "BrockApp - You are offline",
                    "Some features has been disabled. Check the settings to fully use all the features offered"
                )
            }
        }

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        val notification: NotificationCompat.Builder
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (type) {
            true -> {
                notification = util.getNotificationBody(
                    CHANNEL_ID_CONNECTIVITY_NOTIFY,
                    title,
                    content,
                    context
                )
            }

            else -> {
                notification = util.getNotificationBodyWithPendingIntent(
                    CHANNEL_ID_CONNECTIVITY_NOTIFY,
                    title,
                    content,
                    util.getConnectivityPendingIntent(context),
                    context
                )
            }
        }

        manager.notify(ID_CONNECTIVITY_NOTIFY, notification.build())
    }
}