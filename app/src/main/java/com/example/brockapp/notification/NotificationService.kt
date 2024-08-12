package com.example.brockapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.NOTIFICATION_INTENT_FILTER
import com.example.brockapp.R

class NotificationService : Service() {

    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        receiver = object: BroadcastReceiver() {
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
        val filter = IntentFilter(NOTIFICATION_INTENT_FILTER)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Gestisci l'intent se necessario
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
