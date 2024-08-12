package com.example.brockapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.brockapp.R

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)
        getNotificationChannel(context, notificationManager)

        val channelId = "1"

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val type = intent.getStringExtra("type")
        var icon = 0
        when (type) {
            "walk" -> {
                icon = R.drawable.baseline_directions_walk_24
            }
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1, notification)

    }

    private fun getNotificationChannel(
        context: Context,
        notificationManager: NotificationManagerCompat
    ) {

        val channelId = "1"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, "MyChannelName", importance)
        channel.description = "My description"
        notificationManager.createNotificationChannel(channel)
    }
}