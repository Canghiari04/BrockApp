package com.example.brockapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.example.brockapp.service.ConnectivityService

class ConnectivityReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val serviceIntent = Intent(context, ConnectivityService::class.java)
            context.startService(serviceIntent)
        } else {
            Log.d("CONNECTIVITY_RECEIVER", "Weird intent.")
        }
    }
}