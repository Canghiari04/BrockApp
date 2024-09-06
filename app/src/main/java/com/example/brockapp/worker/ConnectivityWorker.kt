package com.example.brockapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.brockapp.CHANNEL_ID_CONNECTIVITY_NOTIFY
import com.example.brockapp.DESCRIPTION_CHANNEL_CONNECTIVITY_NOTIFY
import com.example.brockapp.ID_CONNECTIVITY_NOTIFY
import com.example.brockapp.NAME_CHANNEL_CONNECTIVITY_NOTIFY
import com.example.brockapp.util.NotificationUtil

class ConnectivityWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()

        val type = inputData.getString("type")?.toBoolean()
        if (type == true) sendNotification() else sendErrorNotification()

        return Result.success()
    }

    private fun sendNotification() {
        manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getConnectivityNotification(
            CHANNEL_ID_CONNECTIVITY_NOTIFY,
            applicationContext
        )

        getNotificationChannel()

        manager.notify(ID_CONNECTIVITY_NOTIFY, notification.build())
    }

    private fun sendErrorNotification() {
        manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = util.getConnectivityPendingIntent(applicationContext)
        val notification = util.getErrorConnectivityNotification(
            CHANNEL_ID_CONNECTIVITY_NOTIFY,
            pendingIntent,
            applicationContext)

        getNotificationChannel()

        manager.notify(ID_CONNECTIVITY_NOTIFY, notification.build())
    }

    private fun getNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_CONNECTIVITY_NOTIFY,
            NAME_CHANNEL_CONNECTIVITY_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_CONNECTIVITY_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }
}