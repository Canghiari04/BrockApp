package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.service.StepCounterService
import com.example.brockapp.room.UsersWalkActivityEntity
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.service.HeightDifferenceService
import com.example.brockapp.extraObject.MyServiceConnection

import android.content.Intent
import android.content.Context
import android.content.ServiceConnection

class WalkActivity: ChronometerActivity() {
    private var isStepCounterServiceBound = false
    private var isHeightDifferenceServiceBound = false
    private var stepCounterService: StepCounterService? = null
    private var heightDifferenceService: HeightDifferenceService? = null

    private lateinit var heightDifferenceConnection: ServiceConnection
    private lateinit var stepCounterServiceConnection: ServiceConnection

    override fun onStart() {
        super.onStart()

        if (!isStepCounterServiceBound) {
            stepCounterServiceConnection = MyServiceConnection.createStepCounterService(
                onConnected = { service ->
                    stepCounterService = service
                    isStepCounterServiceBound = true
                },
                onDisconnected = {
                    isStepCounterServiceBound = false
                }
            )
        }

        if (!isHeightDifferenceServiceBound) {
            heightDifferenceConnection = MyServiceConnection.createHeightDifferenceService(
                onConnected = { service ->
                    heightDifferenceService = service
                    isHeightDifferenceServiceBound = true
                },
                onDisconnected = {
                    isHeightDifferenceServiceBound = false
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()

        if (isStepCounterServiceBound || isHeightDifferenceServiceBound) {
            updateActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isStepCounterServiceBound) {
            isStepCounterServiceBound = false
            unbindService(stepCounterServiceConnection)
        }

        if (isHeightDifferenceServiceBound) {
            isHeightDifferenceServiceBound = false
            unbindService(heightDifferenceConnection)
        }
    }

    override fun insertActivity() {
        Intent(this, StepCounterService::class.java).also {
            startService(it)
            bindService(it, stepCounterServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(this, HeightDifferenceService::class.java).also {
            startService(it)
            bindService(it, heightDifferenceConnection, Context.BIND_AUTO_CREATE)
        }

        viewModel.insertWalkActivity(
            UsersWalkActivityEntity(
                username = MyUser.username,
                timestamp = getInstant(),
                arrivalTime = System.currentTimeMillis(),
                exitTime = 0L,
                stepsNumber = 0L,
                heightDifference = 0f
            )
        )
    }

    override fun updateActivity() {
        val stepsNumber = takeIf {
            isStepCounterServiceBound
        }.let {
            stepCounterService?.getSteps()
        }

        val heightDifference = takeIf {
            isHeightDifferenceServiceBound
        }.let {
            heightDifferenceService?.getAltitude()
        }

        viewModel.updateWalkActivity(
            System.currentTimeMillis(),
            stepsNumber ?: 0L,
            heightDifference ?: 0f
        )
    }

    override fun setUpSensors() {
        textViewTitleFirstSensor.text = "Step counter"
        textViewValueFirstSensor.text = "0 steps"
        textViewTitleSecondSensor.text = "Height difference"
        textViewValueSecondSensor.text = "0 m"
    }

    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            stepCounterService?.getSteps().let {
                textViewValueFirstSensor.setText("%d steps".format(it))
            }

            heightDifferenceService?.getAltitude().let {
                textViewValueSecondSensor.setText("%.3f m".format(it))
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "WALK ACTIVITY"
        }
    }
}