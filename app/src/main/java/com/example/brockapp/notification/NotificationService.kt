package com.example.brockapp.notification

import android.Manifest
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
import com.example.brockapp.R

class NotificationService : Service() {

    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val notificationManager = NotificationManagerCompat.from(context)

                // Assicurati che il canale di notifica sia creato
                notificationManager.getNotificationChannel("1")

                val channelId = "1"
                val title = intent.getStringExtra("title")
                val content = intent.getStringExtra("content")
                val type = intent.getStringExtra("type")
                val icon = when (type) {
                    "walk" -> R.drawable.baseline_directions_walk_24
                    else -> R.drawable.circle
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
        }
        val filter = IntentFilter("NOTIFICATION")
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Gestisci l'intent se necessario
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
