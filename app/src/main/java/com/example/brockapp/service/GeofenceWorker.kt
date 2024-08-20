package com.example.brockapp.service

import android.app.NotificationChannel
import com.example.brockapp.R
import com.example.brockapp.GEOFENCE_NOTIFY
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.activity.AuthenticatorActivity

import androidx.work.Worker
import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import androidx.work.WorkerParameters
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class GeofenceWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private lateinit var utilNotification: NotificationUtil
    private lateinit var notificationManager: NotificationManager

    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val channelId = GEOFENCE_NOTIFY
        val channelName = GEOFENCE_NOTIFY

        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, AuthenticatorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId).apply {
            setSmallIcon(R.drawable.baseline_directions_run_24)
            setContentTitle("Sei entrato in una zona speciale")
            setContentText("Inizia a registrare nuove attività!")
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        getNotificationChannel(GEOFENCE_NOTIFY, GEOFENCE_NOTIFY, notificationManager)

        notificationManager.notify(0, notification.build())
    }

    private fun getNotificationChannel(name: String, description: String, notificationManager: NotificationManager) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance)

        channel.description = description
        notificationManager.createNotificationChannel(channel)
    }
}