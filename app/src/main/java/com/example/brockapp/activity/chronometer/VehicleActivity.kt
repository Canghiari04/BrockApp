package com.example.brockapp.activity.chronometer

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.service.DistanceService
import com.example.brockapp.activity.ChronometerActivity
import com.example.brockapp.extraObject.MyServiceConnection
import com.example.brockapp.database.UserVehicleActivityEntity

import android.view.View
import android.os.SystemClock
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection

class VehicleActivity: ChronometerActivity() {
    private var isBound = false
    private var service: DistanceService? = null

    private lateinit var serviceConnection: ServiceConnection

    override fun onStart() {
        super.onStart()

        serviceConnection = MyServiceConnection.createDistanceServiceConnection(
            onConnected = { service ->
                this.service = service
                isBound = true
            },
            onDisconnected = {
                isBound = false
            }
        )
    }

    override fun registerActivity() {
        Intent(this, DistanceService::class.java).also {
            startService(it)
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        viewModel.insertVehicleActivity(
            UserVehicleActivityEntity(
                userId = MyUser.id,
                timestamp = getInstant(),
                arrivalTime = System.currentTimeMillis(),
                exitTime = 0L,
                distanceTravelled = 0.0,
                heightDifference = 0f
            )
        )
    }

    override fun updateActivity() {
        if (isBound) {
            val distanceTravelled = service?.getDistance()
            unbindService(serviceConnection)

            setKindOfSensors()
            viewModel.updateVehicleActivity(System.currentTimeMillis(), distanceTravelled)
        }
    }

    override fun setKindOfSensors() {
        secondTableRow.visibility = View.GONE

        textViewTitleFirstSensor.text = "Distance travelled"
        textViewValueFirstSensor.text = "0.0 km"
    }

    // I used the chronometer view to update the value of the distance done
    override fun setUpChronometer() {
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base

            if ((elapsedTime % 10).toInt() == 0) {
                service?.getDistance()?.div(TO_KM).also {
                    if (it != null) textViewValueFirstSensor.text = ("%.1f km".format(it))
                }
            }
        }
    }

    override fun setTypeActivityTextView() {
        textViewTypeActivity.also {
            it.text = "VEHICLE ACTIVITY"
        }
    }
}