package com.example.brockapp.activity

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE
import com.example.brockapp.R
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

// NON SI SPACCA PER IL DISPOSITIVO DA IMPLEMENTARE PER LE NOTIFICHE

class ActivityActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED) {
                initRecognition()
            } else {
                requestPermission()
            }
        } else {
            if (ContextCompat
                    .checkSelfPermission(
                        this,
                        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
                    ) == PackageManager.PERMISSION_GRANTED) {
                initRecognition()
            } else {
                requestPermission2()
            }
        }
    }

    private fun requestPermission2() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),
            5827
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 5827) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initRecognition()
            } else {
                requestPermission()
            }
        }
    }

    private fun initRecognition() {
        val transitions = defineTransitions()
        val pendingIntent = definePendingIntent()

        val request = ActivityTransitionRequest(transitions)

        if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED) {
            val task = ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(request, pendingIntent)

            task.addOnSuccessListener {
                Log.d("ACTIVITY_ACTIVITY", "Success")
            }

            task.addOnFailureListener { e: Exception ->
                Log.e("ACTIVITY_ACTIVITY", e.toString())
            }
        } else {
            return
        }
    }

    private fun definePendingIntent(): PendingIntent {
        val intent = Intent().apply {
            action = ACTIVITY_RECOGNITION_INTENT_TYPE
        }

        return PendingIntent.getBroadcast(
            this,
            2758,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun defineTransitions(): List<ActivityTransition> {
        val activityTransitionList = mutableListOf<ActivityTransition>()

        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )

        return activityTransitionList
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            5827
        )
    }
}