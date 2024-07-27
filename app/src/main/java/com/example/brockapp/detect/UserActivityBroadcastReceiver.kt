package com.example.brockapp.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.brockapp.database.DbHelper
import com.google.android.gms.location.ActivityTransitionResult

class UserActivityBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
                for(event in result.transitionEvents) {
                    Log.d("DETECT", event.toString())
                }
        } else {
            Log.d("WTF", "WTF")
        }
    }
}
