package com.example.brockapp.fragment

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.R
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

class WalkFragment() : Fragment(R.layout.walk_fragment), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var running = false
    private var stepCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { context ->
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            // Ottieni il sensore di tipo Step Counter
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        }

        val chronometer = view.findViewById<Chronometer>(R.id.walk_chronometer)
        var pauseOffset: Long = 0

        view.findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true

                view.findViewById<Button>(R.id.walk_button_start).isEnabled = false
                view.findViewById<Button>(R.id.walk_button_stop).isEnabled = true

                // Avvia il contapassi
                startStepCounting()
            }

            registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER, -1L)
        }

        view.findViewById<Button>(R.id.walk_button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false

                view.findViewById<Button>(R.id.walk_button_start).isEnabled = true
                view.findViewById<Button>(R.id.walk_button_stop).isEnabled = false

                // Ferma il contapassi
                stopStepCounting()
            }

            registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT, stepCount.toLong())
        }

        view.findViewById<Button>(R.id.walk_button_start).isEnabled = true
        view.findViewById<Button>(R.id.walk_button_stop).isEnabled = false
    }

    private fun startStepCounting() {
        stepCounterSensor?.also { stepSensor ->
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {

            // Il valore del contapassi è il primo elemento nell'array values
            stepCount = event.values[0].toInt()
            Log.d("StepCount", "Passi: $stepCount")

            view?.findViewById<TextView>(R.id.step_count)?.text = stepCount.toString()

        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }


    private fun registerActivity(activityType: Int, transitionType: Int, stepCount : Long) {

        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("stepNumber", stepCount)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}
