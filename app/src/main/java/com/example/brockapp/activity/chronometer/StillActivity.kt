package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.room.UsersStillActivityEntity

import android.view.View
import android.os.SystemClock

class StillActivity: ChronometerActivity() {
    override fun insertActivity() {
        viewModel.insertStillActivity(
            UsersStillActivityEntity(
                username = MyUser.username,
                timestamp = getInstant(),
                arrivalTime = System.currentTimeMillis(),
                exitTime = 0L
            )
        )
    }

    override fun updateActivity() {
        viewModel.updateStillActivity(System.currentTimeMillis())
    }

    override fun setUpSensors() {
        firstTableRow.visibility = View.GONE
        secondTableRow.visibility = View.GONE
    }

    override fun setUpChronometer() {
        var notificationSent = false

        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000).toInt()

            if (hours >= 10 && !notificationSent) {
                sendNotification(
                    "BrockApp - Stand up!",
                    "You have been stilled for more than an hour, do some stretching!"
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