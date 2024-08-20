package com.example.brockapp.receiver

import com.example.brockapp.service.ConnectivityService

import android.util.Log
import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.content.BroadcastReceiver

class ConnectivityReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val serviceIntent = Intent(context, ConnectivityService::class.java)
            context.startService(serviceIntent)
        } else {
            Log.d("CONNECTIVITY_SERVICE", "Foreign intent.")
        }
    }
}