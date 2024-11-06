package com.example.brockapp.singleton

import com.example.brockapp.*
import com.example.brockapp.extraObject.MySharedPreferences
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
        private var status: Boolean = false

        private lateinit var pendingIntent: PendingIntent

        fun getStatus(): Boolean {
            synchronized(this) {
                return status
            }
        }

        fun setStatus(item: Boolean) {
            synchronized(this) {
                status = item
            }
        }

        fun getTask(context: Context): Task<Void>? {
            synchronized(this) {
                return createTask(context)
            }
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
                                status = false
                                pendingIntent.cancel()
                            }

                            task.addOnFailureListener { e: Exception ->
                                Log.e("MY_ACTIVITY_RECOGNITION", e.message.toString())
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

            // I will save inside the shared preferences all the interested activities by Settings
            val transitions = createActivityTransitions(context)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED || transitions.isEmpty()
            ) {
                return null
            }

            val request = ActivityTransitionRequest(transitions)

            return (ActivityRecognition
                .getClient(context)
                .requestActivityTransitionUpdates(request, pendingIntent)
            )
        }

        // Will be fine define by the user the activity he/she is interested in
        private fun createActivityTransitions(context: Context): List<ActivityTransition> {
            val list = mutableListOf(
                MySharedPreferences.getActivity("VEHICLE_ACTIVITY", context),
                MySharedPreferences.getActivity("RUN_ACTIVITY", context),
                MySharedPreferences.getActivity("STILL_ACTIVITY", context),
                MySharedPreferences.getActivity("WALK_ACTIVITY", context)
            ).apply { removeAll { it == DetectedActivity.UNKNOWN } }

            // Inside the shared preferences I will put the same type in .setActivityType
            return mutableListOf<ActivityTransition>().apply {
                list.forEach { item ->
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(item)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build()
                    )
                    add(
                        ActivityTransition.Builder()
                            .setActivityType(item)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build()
                    )
                }
            }
        }

        private fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ActivityRecognitionReceiver::class.java).apply {
                action = ACTIVITY_RECOGNITION_INTENT_TYPE
            }

            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ACTIVITY_RECOGNITION_BROADCAST_RECEIVER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }
    }
}