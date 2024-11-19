package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.service.DistanceService
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.room.UsersVehicleActivityEntity
import com.example.brockapp.extraObject.MyServiceConnection

import android.view.View
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection

class VehicleActivity: ChronometerActivity() {

    private var isBound = false
    private var service: DistanceService? = null

    private lateinit var serviceConnection: ServiceConnection

    override fun onStart() {
        super.onStart()

        if (!isBound) {
            serviceConnection = MyServiceConnection.createDistanceServiceConnection(
                onConnected = { service ->
                    this.service = service
                    isBound = true

                    this.service?.resetDistance()
                },
                onDisconnected = {
                    isBound = false
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()

        if (isBound) {
            updateActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isBound) {
            isBound = false
            unbindService(serviceConnection)
        }
    }

    override fun insertActivity() {
        Intent(this, DistanceService::class.java).also {
            startService(it)
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        viewModel.insertVehicleActivity(
            UsersVehicleActivityEntity(
                username = MyUser.username,
                timestamp = getInstant(),
                arrivalTime = System.currentTimeMillis(),
                exitTime = 0L,
                distanceTravelled = 0.0
            )
        )
    }

    override fun updateActivity() {
        val distanceTravelled = takeIf {
            isBound
        }.let {
            service?.getDistance()
        }

        viewModel.updateVehicleActivity(
            System.currentTimeMillis(),
            distanceTravelled ?: 0.0
        )
    }

    override fun setUpSensors() {
        secondTableRow.visibility = View.GONE

        textViewTitleFirstSensor.text = "Distance travelled"
        textViewValueFirstSensor.text = "0 km"
    }

    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            service?.getDistance()?.div(TO_KM).let {
                textViewValueFirstSensor.text = ("%.3f km".format(it))
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "VEHICLE ACTIVITY"
        }
    }
}