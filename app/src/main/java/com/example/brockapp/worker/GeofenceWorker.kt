package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil

import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationChannel
import android.app.NotificationManager

class GeofenceWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()
        sendNotification()

        return Result.success()
    }

    private fun sendNotification() {
        manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getGeofenceNotification(CHANNEL_ID_GEOFENCE_NOTIFY, applicationContext)
        getNotificationChannel()

        manager.notify(ID_GEOFENCE_NOTIFY, notification.build())
    }

    private fun getNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID_GEOFENCE_NOTIFY, NAME_CHANNEL_GEOFENCE_NOTIFY, importance)

        channel.apply {
            description = DESCRIPTION_CHANNEL_GEOFENCE_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }
}