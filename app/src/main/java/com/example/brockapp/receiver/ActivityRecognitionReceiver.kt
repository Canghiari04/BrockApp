package com.example.brockapp.receiver

import com.example.brockapp.*
import com.google.android.gms.location.DetectedActivity
import com.example.brockapp.service.ActivityRecognitionService

import android.util.Log
import java.time.Instant
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import java.time.format.DateTimeFormatter

class ActivityRecognitionReceiver: BroadcastReceiver() {
    private lateinit var serviceIntent: Intent

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTIVITY_RECOGNITION_INTENT_TYPE) {
            try {
                val serviceIntent = defineIntent(intent, context)
                context.startService(serviceIntent)
            } catch (e: Exception) {
                Log.e("ACTIVITY_RECOGNITION_RECEIVER", e.toString())
            }
        } else {
            Log.d("ACTIVITY_RECOGNITION_RECEIVER", "Weird intent.")
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