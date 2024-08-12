package com.example.brockapp.notification

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.NOTIFICATION_INTENT_FILTER

class NotificationService : Service() {

    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        receiver = NotificationBroadcastReceiver()
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
