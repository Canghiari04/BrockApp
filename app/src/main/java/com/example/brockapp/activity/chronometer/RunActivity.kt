package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.service.DistanceService
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.database.UserRunActivityEntity
import com.example.brockapp.service.HeightDifferenceService
import com.example.brockapp.extraObject.MyServiceConnection

import android.os.SystemClock
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection

class RunActivity: ChronometerActivity() {
    private var distanceService: DistanceService? = null
    private var heightDifferenceService: HeightDifferenceService? = null

    private lateinit var distanceServiceConnection: ServiceConnection
    private lateinit var heightDifferenceConnection: ServiceConnection

    override fun onStart() {
        super.onStart()

        distanceServiceConnection = MyServiceConnection.createDistanceServiceConnection(
            onConnected = { service ->
                distanceService = service
            }
        )

        heightDifferenceConnection = MyServiceConnection.createHeightDifferenceService(
            onConnected = { service ->
                heightDifferenceService = service
            }
        )
    }

    override fun registerActivity() {
        Intent(this, DistanceService::class.java).also {
            startService(it)
            bindService(it, distanceServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(this, HeightDifferenceService::class.java).also {
            startService(it)
            bindService(it, heightDifferenceConnection, Context.BIND_AUTO_CREATE)
        }

        viewModel.insertRunActivity(
            UserRunActivityEntity(
                userId = MyUser.id,
                timestamp = getInstant(),
                arrivalTime = System.currentTimeMillis(),
                exitTime = 0L,
                distanceDone = 0.0,
                heightDifference = 0f
            )
        )
    }

    override fun updateActivity() {
        val distancedDone = distanceService?.getDistance()
        unbindService(distanceServiceConnection)

        val heightDifference = heightDifferenceService?.getAltitude()
        unbindService(heightDifferenceConnection)

        setKindOfSensors()
        viewModel.updateRunActivity(
            System.currentTimeMillis(),
            distancedDone,
            heightDifference
        )
    }

    override fun setKindOfSensors() {
        textViewTitleFirstSensor.text = "Distance run"
        textViewValueFirstSensor.text = "0 km"
        textViewTitleSecondSensor.text = "Height difference"
        textViewValueSecondSensor.text = "0 m"
    }

    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base

            if ((elapsedTime % 10).toInt() == 0) {
                val data = (distanceService?.getDistance()?.div(TO_KM))

                textViewValueFirstSensor.text = ("%.3f km".format(data))
                textViewValueSecondSensor.text = heightDifferenceService?.getAltitude().toString()
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "RUN ACTIVITY"
        }
    }
}