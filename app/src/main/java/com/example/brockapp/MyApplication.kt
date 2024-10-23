package com.example.brockapp

import com.example.brockapp.worker.SyncDataWorker
import com.example.brockapp.extraObject.MySharedPreferences

import android.app.Application
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.core.app.NotificationManagerCompat

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Periodic worker define to sync data if the permission has been allowed
        if (MySharedPreferences.checkService("DUMP_DATABASE", this)) scheduleSyncPeriodic()

        val notificationManager = NotificationManagerCompat.from(this)

        // Setup all the Notification Channel
        memoNotificationChannel(notificationManager)
        syncDataNotificationChannel(notificationManager)
        geofencingNotificationChannel(notificationManager)
        connectivityNotificationChannel(notificationManager)
        activityRecognitionNotificationChannel(notificationManager)
    }

    private fun scheduleSyncPeriodic() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncDataWorker>(2, TimeUnit.HOURS)
            .setConstraints(constraint)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncDataWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun memoNotificationChannel(manager: NotificationManagerCompat) {
        val channel = NotificationChannel(
            CHANNEL_ID_MEMO_NOTIFY,
            NAME_CHANNEL_ID_MEMO_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_ID_MEMO_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }

    private fun syncDataNotificationChannel(manager: NotificationManagerCompat) {
        val channel = NotificationChannel(
            CHANNEL_ID_SYNC_DATA_NOTIFY,
            NAME_CHANNEL_SYNC_DATA_NOTIFY,
            NotificationManager.IMPORTANCE_LOW
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_SYNC_DATA_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }

    private fun geofencingNotificationChannel(manager: NotificationManagerCompat) {
        val channel = NotificationChannel(
            CHANNEL_ID_GEOFENCE_NOTIFY,
            NAME_CHANNEL_GEOFENCE_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_GEOFENCE_NOTIFY
        }

        manager.createNotificationChannel(channel)
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