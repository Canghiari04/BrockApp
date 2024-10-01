package com.example.brockapp.singleton

import com.example.brockapp.*
import com.example.brockapp.receiver.ActivityRecognitionReceiver

import android.Manifest
import android.util.Log
import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransitionRequest

class MyActivityRecognition private constructor() {
    companion object {
        @Volatile
        private var task: Task<Void>? = null

        private lateinit var pendingIntent: PendingIntent

        fun getTask(context: Context): Task<Void> {
            synchronized(this) {
                if (task == null) {
                    task = createTask(context)
                }
            }

            return task!!
        }

        fun removeTask(context: Context) {
            synchronized(this) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    run {
                        if (::pendingIntent.isInitialized) {
                            val task =
                                ActivityRecognition
                                    .getClient(context)
                                    .removeActivityTransitionUpdates(pendingIntent)

                            task.addOnSuccessListener {
                                pendingIntent.cancel()
                            }

                            task.addOnFailureListener { e: Exception ->
                                Log.e("MY_ACTIVITY_RECOGNITION", e.message!!)
                            }
                        }
                    }
                } else {
                    Log.wtf("MY_ACTIVITY_RECOGNITION", "Permission activity recognition denied")
                }
            }
        }

        private fun createTask(context: Context): Task<Void>? {
            pendingIntent = createPendingIntent(context)

            val transitions = createActivityTransitions()
            val request = ActivityTransitionRequest(transitions)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }

            return (
                    ActivityRecognition
                        .getClient(context)
                        .requestActivityTransitionUpdates(request, pendingIntent)
            )
        }

        // Will be fine define by the user the activity he/she is interested in
        private fun createActivityTransitions(): List<ActivityTransition> {
            return mutableListOf<ActivityTransition>()
                .apply {
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.STILL)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.STILL)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.WALKING)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.WALKING)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.RUNNING)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.RUNNING)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.IN_VEHICLE)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(DetectedActivity.IN_VEHICLE)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build()
                    )
                }
        }

        private fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ActivityRecognitionReceiver::class.java).apply {
                action = ACTIVITY_RECOGNITION_INTENT_TYPE
            }

            return PendingIntent.getBroadcast(
                context,
                46,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }
    }
}