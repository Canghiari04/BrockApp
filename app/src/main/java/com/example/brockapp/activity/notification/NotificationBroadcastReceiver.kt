package com.example.brockapp.activity.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)
        getNotificationChannel(context, notificationManager)

        val notification = intent.getParcelableExtra("notification", Notification::class.java)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (notification != null) {
            notificationManager.notify(1, notification)
        }
    }

    private fun getNotificationChannel(context: Context, notificationManager : NotificationManagerCompat){

        val channelId = "1"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, "MyChannelName", importance)
        channel.description = "My description"
        notificationManager.createNotificationChannel(channel)
    }
}