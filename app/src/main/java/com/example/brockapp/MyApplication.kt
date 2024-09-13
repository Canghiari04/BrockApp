package com.example.brockapp

import com.example.brockapp.singleton.MyGeofence

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Define the Pending Intent for service in background
        MyGeofence.initPendingIntent(this)

        val notificationManager = NotificationManagerCompat.from(this)

        // Setup all the Notification Channel
        connectivityNotificationChannel(notificationManager)
        geofencingNotificationChannel(notificationManager)
        activityRecognitionNotificationChannel(notificationManager)
    }

    private fun connectivityNotificationChannel(manager: NotificationManagerCompat) {
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

    private fun geofencingNotificationChannel(manager: NotificationManagerCompat) {
        val channel = NotificationChannel(
            CHANNEL_ID_GEOFENCE_NOTIFY,
            NAME_CHANNEL_GEOFENCE_NOTIFY,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_GEOFENCE_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }

    private fun activityRecognitionNotificationChannel(manager: NotificationManagerCompat) {
        val channel = NotificationChannel(
            CHANNEL_ID_ACTIVITY_NOTIFY,
            NAME_CHANNEL_ACTIVITY_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_ACTIVITY_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }
}