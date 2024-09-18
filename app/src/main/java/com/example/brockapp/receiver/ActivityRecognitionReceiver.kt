package com.example.brockapp.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.brockapp.*
import com.example.brockapp.service.ActivityRecognitionService
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


class ActivityRecognitionReceiver: BroadcastReceiver() {
    private lateinit var serviceIntent: Intent

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ACTIVITY_RECOGNITION_RECEIVER", "Ricevuto broadcast con azione: ${intent.action}")
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null) {
                for (event in result.transitionEvents) {
                    Toast.makeText(context, "CIAO ESTHER", Toast.LENGTH_LONG).show()
                    Log.d("ACTIVITY_RECOGNITION_RECEIVER", event.activityType.toString())
                }
            } else {
                Log.d("ACTIVITY_RECOGNITION_RECEIVER", "Null result")
            }
        }
    }

    fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ActivityRecognitionReceiver::class.java).apply {
            action = ACTIVITY_RECOGNITION_INTENT_TYPE
        }

        return PendingIntent.getBroadcast(
            context,
            46,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}