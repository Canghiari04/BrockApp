package com.example.brockapp.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult

class UserActivityBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                for (event in it.transitionEvents) {
                    val activityType = UserActivityTransitionManager.getActivityType(event.activityType)
                    val transitionType = UserActivityTransitionManager.getTransitionType(event.transitionType)
                    // Salvare nel database
                    saveActivityToDatabase(context, activityType, transitionType, event.elapsedRealTimeNanos)
                }
            }
        }
    }

    private fun saveActivityToDatabase(context: Context, activityType: String, transitionType: String, timestamp: Long) {
        // Implementa il salvataggio nel database
        // Puoi usare una coroutine o qualsiasi altro meccanismo di threading per salvare nel database
    }
}
