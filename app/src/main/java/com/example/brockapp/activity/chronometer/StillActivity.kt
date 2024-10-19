package com.example.brockapp.activity.chronometer

import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.service.ActivityRecognitionService

import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.location.DetectedActivity

class StillActivity: ChronometerActivity() {
    override fun registerActivity() {
        Intent(this, ActivityRecognitionService::class.java).also {
            it.action = ActivityRecognitionService.Actions.START.toString()

            it.putExtra("ACTIVITY_TYPE", DetectedActivity.STILL)
            it.putExtra("ARRIVAL_TIME", System.currentTimeMillis())

            startService(it)
        }
    }

    override fun updateActivity() {
        Intent(this, ActivityRecognitionService::class.java).also {
            it.action = ActivityRecognitionService.Actions.UPDATE.toString()

            it.putExtra("ACTIVITY_TYPE", DetectedActivity.STILL)
            it.putExtra("EXIT_TIME", System.currentTimeMillis())

            startService(it)
        }
    }

    override fun setUpChronometer() {
        var notificationSent = false

        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000).toInt()

            if (hours >= 10 && !notificationSent) {
                sendNotification(
                    "BrockApp - Stand up!",
                    "You have been stilled for more than an hour, do some stretching"
                )

                notificationSent = true
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "STILL ACTIVITY"
        }
    }
}