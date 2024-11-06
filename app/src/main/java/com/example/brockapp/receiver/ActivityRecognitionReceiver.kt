package com.example.brockapp.receiver

import com.example.brockapp.*
import com.example.brockapp.service.ActivityRecognitionService

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import com.google.android.gms.location.ActivityTransitionResult

class ActivityRecognitionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTIVITY_RECOGNITION_INTENT_TYPE) {
            if (ActivityTransitionResult.hasResult(intent)) {
                val result = ActivityTransitionResult.extractResult(intent)!!
                val events = result.transitionEvents

                for (event in events) {
                    val type = event.activityType
                    val transition = event.transitionType

                    startService(type, transition, context)
                }
            }
        } else {
            Log.d("ACTIVITY_RECOGNITION_RECEIVER", "Intent action not recognized")
        }
    }

    private fun startService(type: Int, transition: Int, context: Context): Intent {
        return Intent(context, ActivityRecognitionService::class.java).also {
            val key: String
            val action: String

            if (transition == 0) {
                action = ActivityRecognitionService.Actions.INSERT.toString()
                key = "ARRIVAL_TIME"
            } else {
                action = ActivityRecognitionService.Actions.UPDATE.toString()
                key = "EXIT_TIME"
            }

            it.action = action
            it.putExtra("ACTIVITY_TYPE", type)
            it.putExtra(key, System.currentTimeMillis())

            context.startService(it)
        }
    }
}