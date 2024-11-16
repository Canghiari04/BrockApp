package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender

import android.util.Log
import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ConnectivityWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), NotificationSender {
    private var isConnected = false

    override fun doWork(): Result {
        isConnected = inputData.getBoolean("IS_CONNECTED", false)

        if (isConnected != MyNetwork.isConnected) {
            MyNetwork.isConnected = isConnected

            when (isConnected) {
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
        } else {
            Log.d("CONNECTIVITY_WORKER", "Insignificant network change")
        }

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: NotificationCompat.Builder
        val notificationUtil = NotificationUtil()

        when (isConnected) {
            true -> {
                val icon = R.drawable.baseline_signal_wifi_statusbar_4_bar_24

                notification = notificationUtil.getNotificationBody(
                    CHANNEL_ID_CONNECTIVITY_WORKER,
                    icon,
                    title,
                    content,
                    context
                )
            }

            else -> {
                val icon = R.drawable.baseline_signal_wifi_statusbar_null_24

                notification = notificationUtil.getNotificationBodyWithPendingIntent(
                    CHANNEL_ID_CONNECTIVITY_WORKER,
                    icon,
                    title,
                    content,
                    notificationUtil.getConnectivityPendingIntent(context),
                    context
                )
            }
        }

        manager.notify(ID_CONNECTIVITY_WORKER_NOTIFY, notification.build())
    }
}