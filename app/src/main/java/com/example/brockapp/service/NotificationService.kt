package com.example.brockapp.service

import com.example.brockapp.GEOFENCE_NOTIFY
import com.example.brockapp.CONNECTIVITY_NOTIFY
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.NOTIFICATION_INTENT_TYPE
import com.example.brockapp.ACTIVITY_RECOGNITION_NOTIFY

import android.util.Log
import android.Manifest
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.Context
import android.app.Notification
import android.content.IntentFilter
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.ActivityCompat
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat

class NotificationService : Service() {
    private lateinit var notification: Notification
    private lateinit var receiver: BroadcastReceiver
    private lateinit var utilNotification: NotificationUtil
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == NOTIFICATION_INTENT_TYPE) {
                    notificationManager = NotificationManagerCompat.from(context)
                    utilNotification = NotificationUtil()

                    val type = intent.getStringExtra("typeNotify")

                    when (type) {
                        ACTIVITY_RECOGNITION_NOTIFY -> {
                            getNotificationChannel(ACTIVITY_RECOGNITION_NOTIFY, ACTIVITY_RECOGNITION_NOTIFY, notificationManager)
                            notification = utilNotification.getActivityRecognitionNotification(ACTIVITY_RECOGNITION_NOTIFY, context, intent).build()
                        }
                        GEOFENCE_NOTIFY -> {
                            getNotificationChannel(GEOFENCE_NOTIFY, GEOFENCE_NOTIFY, notificationManager)
                            notification = utilNotification.getGeofenceNotification(GEOFENCE_NOTIFY, context, intent).build()
                        }
                        CONNECTIVITY_NOTIFY -> {

                        }
                    }

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notificationManager.notify(0, notification)
                    } else {
                        Log.d("WTF", "WTF")
                    }
                }
            }
        }

        registerReceiver(receiver, IntentFilter(NOTIFICATION_INTENT_TYPE), RECEIVER_NOT_EXPORTED)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotificationChannel(id: String, description: String, notificationManager: NotificationManagerCompat) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(id, id, importance)

        channel.description = description
        notificationManager.createNotificationChannel(channel)
    }
}