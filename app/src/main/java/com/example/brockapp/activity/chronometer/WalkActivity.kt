package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.service.StepCounterService
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.service.HeightDifferenceService
import com.example.brockapp.extraObject.MyServiceConnection

import android.os.SystemClock
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

        stepCounterServiceConnection = MyServiceConnection.createStepCounterService(
            onConnected = { service ->
                stepCounterService = service
                isStepCounterServiceBound = true
            },
            onDisconnected = {
                isStepCounterServiceBound = false
            }
        )

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

    override fun registerActivity() {
        Intent(this, StepCounterService::class.java).also {
            startService(it)
            bindService(it, stepCounterServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(this, HeightDifferenceService::class.java).also {
            startService(it)
            bindService(it, heightDifferenceConnection, Context.BIND_AUTO_CREATE)
        }

        viewModel.insertWalkActivity(
            UserWalkActivityEntity(
                userId = MyUser.id,
                timestamp = getInstant(),
                arrivalTime = System.currentTimeMillis(),
                exitTime = 0L,
                stepNumber = 0L,
                heightDifference = 0f
            )
        )
    }

    override fun updateActivity() {
        if (isStepCounterServiceBound && isHeightDifferenceServiceBound) {
            val stepNumber = stepCounterService?.getSteps()
            unbindService(stepCounterServiceConnection)

            val heightDifference = heightDifferenceService?.getAltitude()
            unbindService(heightDifferenceConnection)

            setKindOfSensors()
            viewModel.updateWalkActivity(
                System.currentTimeMillis(),
                stepNumber,
                heightDifference
            )
        }
    }

    override fun setKindOfSensors() {
        textViewTitleFirstSensor.text = "Step counter"
        textViewValueFirstSensor.text = "0 steps"
        textViewTitleSecondSensor.text = "Height difference"
        textViewValueSecondSensor.text = "0 m"
    }

    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base

            if ((elapsedTime % 10).toInt() == 0) {
                stepCounterService?.getSteps().also {
                    if (it != null) textViewValueFirstSensor.setText("%d steps".format(it))
                }

                heightDifferenceService?.getAltitude().also {
                    if (it != null) textViewValueSecondSensor.setText("%.3f m".format(it))
                }
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "WALK ACTIVITY"
        }
    }
}