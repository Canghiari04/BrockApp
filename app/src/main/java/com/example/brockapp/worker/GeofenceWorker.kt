package com.example.brockapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.brockapp.CHANNEL_ID_GEOFENCE_NOTIFY
import com.example.brockapp.DESCRIPTION_CHANNEL_GEOFENCE_NOTIFY
import com.example.brockapp.ID_GEOFENCE_NOTIFY
import com.example.brockapp.NAME_CHANNEL_GEOFENCE_NOTIFY
import com.example.brockapp.util.NotificationUtil

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

        val pendingIntent = util.getGeofencePendingIntent(applicationContext)
        val notification = util.getGeofenceNotification(CHANNEL_ID_GEOFENCE_NOTIFY, pendingIntent, applicationContext)

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