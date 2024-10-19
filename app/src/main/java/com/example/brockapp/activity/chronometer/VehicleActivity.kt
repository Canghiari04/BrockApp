package com.example.brockapp.activity.chronometer

import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.service.ActivityRecognitionService

import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.location.DetectedActivity

class VehicleActivity: ChronometerActivity() {
    override fun registerActivity() {
        Intent(this, ActivityRecognitionService::class.java).also {
            it.action = ActivityRecognitionService.Actions.START.toString()

            it.putExtra("ACTIVITY_TYPE", DetectedActivity.IN_VEHICLE)
            it.putExtra("ARRIVAL_TIME", System.currentTimeMillis())

            startService(it)
        }
    }

    override fun updateActivity() {
        Intent(this, ActivityRecognitionService::class.java).also {
            it.action = ActivityRecognitionService.Actions.UPDATE.toString()

            it.putExtra("ACTIVITY_TYPE", DetectedActivity.IN_VEHICLE)
            it.putExtra("EXIT_TIME", System.currentTimeMillis())

            startService(it)
        }
    }

    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000).toInt()

            // TODO
            /*
             * I need a condition to check if is passed more than an hour, or few hours (2 - 3)
             */
            if (hours >= 10) {
//                sendNotification(
//                    "BrockApp - Take a break",
//                    "You have been driving for a while, take a pause"
//                )
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "VEHICLE ACTIVITY"
        }
    }
}