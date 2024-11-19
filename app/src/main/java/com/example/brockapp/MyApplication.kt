package com.example.brockapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat

class MyApplication: Application() {

    private lateinit var manager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()

        manager = NotificationManagerCompat.from(this)

        supabaseNotificationChannel()
        distanceNotificationChannel()
        stepCounterNotificationChannel()
        heightDifferenceNotificationChannel()

        memoNotificationChannel()
        geofencingNotificationChannel()
        connectivityNotificationChannel()
        activityRecognitionNotificationChannel()
    }

    private fun supabaseNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_SUPABASE_SERVICE,
            NAME_CHANNEL_SUPABASE_SERVICE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            it.description = DESCRIPTION_CHANNEL_SUPABASE_SERVICE
            manager.createNotificationChannel(it)
        }
    }

    private fun distanceNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_DISTANCE_SERVICE,
            NAME_CHANNEL_DISTANCE_SERVICE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            it.description = DESCRIPTION_CHANNEL_DISTANCE_SERVICE
            manager.createNotificationChannel(it)
        }
    }

    private fun stepCounterNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_STEP_COUNTER_SERVICE,
            NAME_CHANNEL_STEP_COUNTER_SERVICE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            it.description = DESCRIPTION_CHANNEL_STEP_COUNTER_SERVICE
            manager.createNotificationChannel(it)
        }
    }

    private fun heightDifferenceNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_HEIGHT_DIFFERENCE_SERVICE,
            NAME_CHANNEL_HEIGHT_DIFFERENCE_SERVICE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            it.description = DESCRIPTION_CHANNEL_HEIGHT_DIFFERENCE_SERVICE
            manager.createNotificationChannel(it)
        }
    }

    private fun memoNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_MEMO_WORKER,
            NAME_CHANNEL_MEMO_WORKER,
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.description = DESCRIPTION_CHANNEL_MEMO_WORKER
            manager.createNotificationChannel(it)
        }
    }

    private fun geofencingNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_GEOFENCE_WORKER,
            NAME_CHANNEL_GEOFENCE_WORKER,
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.description = DESCRIPTION_CHANNEL_GEOFENCE_WORKER
            manager.createNotificationChannel(it)
        }
    }

    private fun connectivityNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_CONNECTIVITY_WORKER,
            NAME_CHANNEL_CONNECTIVITY_WORKER,
            NotificationManager.IMPORTANCE_LOW
        ).also {
            it.description = DESCRIPTION_CHANNEL_CONNECTIVITY_WORKER
            manager.createNotificationChannel(it)
        }
    }

    private fun activityRecognitionNotificationChannel() {
        NotificationChannel(
            CHANNEL_ID_ACTIVITY_RECOGNITION_WORKER,
            NAME_CHANNEL_ACTIVITY_RECOGNITION_WORKER,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            it.description = DESCRIPTION_CHANNEL_ACTIVITY_RECOGNITION_WORKER
            manager.createNotificationChannel(it)
        }
    }
}