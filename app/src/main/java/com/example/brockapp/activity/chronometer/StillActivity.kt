package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.room.UsersStillActivityEntity

import android.view.View

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

    override fun setUpChronometer() { }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "STILL ACTIVITY"
        }
    }
}