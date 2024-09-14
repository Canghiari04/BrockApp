package com.example.brockapp.receiver

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
                    Log.d("ACTIVITY_RECOGNITION_RECEIVER", event.activityType.toString())
                }
            } else {
                Log.d("ACTIVITY_RECOGNITION_RECEIVER", "Null result")
            }
        }
    }

    private fun defineIntent(intent: Intent, context: Context): Intent {
        val activityType = intent.getIntExtra("activityType", -1)
        val transitionType = intent.getIntExtra("transitionType", -1)
        val timestamp = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).withZone(ZoneOffset.UTC).format(Instant.now())

        when (activityType) {
            DetectedActivity.STILL -> {
                serviceIntent = Intent(context, ActivityRecognitionService::class.java).apply {
                    putExtra("ACTIVITY_TYPE", activityType)
                    putExtra("TRANSITION_TYPE", transitionType)
                    putExtra("TIMESTAMP", timestamp)
                }
            }

            DetectedActivity.IN_VEHICLE -> {
                val distanceTravelled = intent.getDoubleExtra("distanceTravelled", -1.0)

                serviceIntent = Intent(context, ActivityRecognitionService::class.java).apply {
                    putExtra("ACTIVITY_TYPE", activityType)
                    putExtra("TRANSITION_TYPE", transitionType)
                    putExtra("TIMESTAMP", timestamp)
                    putExtra("DISTANCE_TRAVELLED", distanceTravelled)
                }
            }

            DetectedActivity.WALKING -> {
                val stepNumber = intent.getLongExtra("stepNumber", -1)

                serviceIntent = Intent(context, ActivityRecognitionService::class.java).apply {
                    putExtra("ACTIVITY_TYPE", activityType)
                    putExtra("TRANSITION_TYPE", transitionType)
                    putExtra("TIMESTAMP", timestamp)
                    putExtra("STEP_NUMBER", stepNumber)
                }
            }
        }

        return serviceIntent
    }
}