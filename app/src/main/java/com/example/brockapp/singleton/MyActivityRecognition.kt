package com.example.brockapp.singleton

import com.example.brockapp.*
import com.example.brockapp.receiver.ActivityRecognitionReceiver

import android.Manifest
import android.util.Log
import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransitionRequest

object MyActivityRecognition {
    private lateinit var pendingIntent: PendingIntent
    private lateinit var request: ActivityTransitionRequest

    fun initActivityRecognition(context: Context) {
        val transitions = defineTransitions()

        request = ActivityTransitionRequest(transitions)
        pendingIntent = definePendingIntent(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            val task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, pendingIntent)

            // TODO NOTIFICHE CHE TI AVVISANO SE ANDATO TUTTO BENE

            task.addOnSuccessListener {
                Log.d("MY_ACTIVITY_RECOGNITION", "Background service activated")
            }

            task.addOnFailureListener { e: Exception ->
                Log.e("MY_ACTIVITY_RECOGNITION", e.toString())
            }
        } else {
            Log.wtf("MY_ACTIVITY_RECOGNITION", "Denied Activity Recognition permission")
        }
    }

    fun stopActivityRecognition() {

    }

    private fun defineTransitions(): List<ActivityTransition> {
        val transitions = mutableListOf<ActivityTransition>()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        return transitions
    }

    private fun definePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ActivityRecognitionReceiver::class.java).apply {
            ACTIVITY_RECOGNITION_INTENT_TYPE
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ACTIVITY_RECOGNITION_BROADCAST_RECEIVER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}