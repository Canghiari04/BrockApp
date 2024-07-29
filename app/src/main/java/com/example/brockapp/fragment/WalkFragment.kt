package com.example.brockapp.fragment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.brockapp.R

class WalkFragment : Fragment(R.layout.start_stop_activity_fragment) {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        activity?.let { context ->
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            // Ottieni il sensore di tipo Step Counter
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        }
    }
}
