package com.example.brockapp.activity.chronometer

import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.service.ActivityRecognitionService

import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.location.DetectedActivity

class RunActivity: ChronometerActivity() {
    override fun registerActivity() {
        Intent(this, ActivityRecognitionService::class.java).also {
            it.action = ActivityRecognitionService.Actions.START.toString()

            it.putExtra("ACTIVITY_TYPE", DetectedActivity.RUNNING)
            it.putExtra("ARRIVAL_TIME", System.currentTimeMillis())

            startService(it)
        }
    }

    override fun updateActivity() {
        Intent(this, ActivityRecognitionService::class.java).also {
            it.action = ActivityRecognitionService.Actions.UPDATE.toString()

            it.putExtra("ACTIVITY_TYPE", DetectedActivity.RUNNING)
            it.putExtra("EXIT_TIME", System.currentTimeMillis())

            startService(it)
        }
    }

    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000).toInt()

            if (hours >= 10) {
                sendNotification(
                    "BrockApp - Keep running!",
                    "You are doing great, keep running"
                )
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "RUN ACTIVITY"
        }
    }
}