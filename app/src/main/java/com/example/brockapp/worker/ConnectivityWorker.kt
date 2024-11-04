package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.interfaces.SchedulePeriodicWorkerImpl

import android.util.Log
import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ConnectivityWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), NotificationSender {
    private var isConnected = false

    override fun doWork(): Result {
        val scheduleWorkerUtil = SchedulePeriodicWorkerImpl(context)

        isConnected = inputData.getBoolean("IS_CONNECTED", false)

        if (isConnected != MyNetwork.isConnected) {
            MyNetwork.isConnected = isConnected

            when (isConnected) {
                true -> {
                    sendNotification(
                        "BrockApp - You are online again",
                        "The disabled features are now active. Resume using all the functionalities to monitor your progress"
                    )

                    scheduleWorkerUtil.scheduleSyncPeriodic()
                }

                else -> {
                    sendNotification(
                        "BrockApp - You are offline",
                        "Some features has been disabled. Check the settings to fully use all the features offered"
                    )

                    scheduleWorkerUtil.deleteSyncPeriodic()
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
                notification = notificationUtil.getNotificationBody(
                    CHANNEL_ID_CONNECTIVITY_NOTIFY,
                    title,
                    content,
                    context
                )
            }

            else -> {
                notification = notificationUtil.getNotificationBodyWithPendingIntent(
                    CHANNEL_ID_CONNECTIVITY_NOTIFY,
                    title,
                    content,
                    notificationUtil.getConnectivityPendingIntent(context),
                    context
                )
            }
        }

        manager.notify(ID_CONNECTIVITY_NOTIFY, notification.build())
    }
}