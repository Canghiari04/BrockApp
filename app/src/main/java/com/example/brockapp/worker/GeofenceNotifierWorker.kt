package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.interfaces.NotificationSender

import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationManager

class GeofenceNotifierWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), NotificationSender {

    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()

        val nameLocation = inputData.getString("LOCATION_NAME")

        sendNotification(
            "BrockApp - You are near $nameLocation",
            "Be sure to track your activities. Open the app and record your movements so you don't lose your progress"
        )

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getNotificationBodyWithPendingIntent(
            CHANNEL_ID_GEOFENCE_WORKER,
            R.drawable.marker_icon,
            title,
            content,
            util.getGeofencePendingIntent(context),
            context
        )

        manager.notify(ID_GEOFENCE_WORKER_NOTIFY, notification.build())
    }
}