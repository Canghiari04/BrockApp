package com.example.brockapp.detect

import com.example.brockapp.REQUEST_CODE_ACTIVITY_BROADCAST_RECEIVER

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.brockapp.REQUEST_CODE_BROADCAST_RECEIVER
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

class UserActivityTransitionManager(context: Context?) {
    /**
     * Restituisce il pending intent necessario per risvegliare il broadcast receiver.
     */
    fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, UserActivityBroadcastReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ACTIVITY_BROADCAST_RECEIVER,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun getRequest(): ActivityTransitionRequest {
        return ActivityTransitionRequest(getTransitions())
    }

    /**
     * Restituisce la lista delle transition activity di interesse.
     */
    private fun getTransitions(): List<ActivityTransition> {
        val transitions = mutableListOf<ActivityTransition>()

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

        return transitions
    }
}