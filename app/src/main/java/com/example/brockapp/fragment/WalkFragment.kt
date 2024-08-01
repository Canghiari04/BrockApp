package com.example.brockapp.fragment

import com.example.brockapp.database.DbHelper
import android.Manifest
import android.content.Context
import android.util.Log
import android.view.View
import android.os.Bundle
import android.widget.Button
import com.example.brockapp.R
import android.os.SystemClock
import android.content.Intent
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityRecognition
import com.example.brockapp.detect.UserActivityTransitionManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class WalkFragment() : Fragment(R.layout.start_stop_activity_fragment), SensorEventListener {

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

        val transitionManager = UserActivityTransitionManager(requireContext())
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        var pauseOffset: Long = 0

        view.findViewById<Button>(R.id.button_start).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true

                view.findViewById<Button>(R.id.button_start).isEnabled = false
                view.findViewById<Button>(R.id.button_stop).isEnabled = true

                // Avvia il contapassi
                startStepCounting()
            }

            startDetection(transitionManager)
        }

        view.findViewById<Button>(R.id.button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false

                view.findViewById<Button>(R.id.button_start).isEnabled = true
                view.findViewById<Button>(R.id.button_stop).isEnabled = false

                // Ferma il contapassi
                stopStepCounting()
            }

            simulateActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        }

        view.findViewById<Button>(R.id.button_start).isEnabled = true
        view.findViewById<Button>(R.id.button_stop).isEnabled = false
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

        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    private fun startDetection(transitionManager: UserActivityTransitionManager) {
        val request = transitionManager.getRequest()
        val myPendingIntentActivityRecognition = transitionManager.getPendingIntent(requireContext())

        // Check richiesto obbligatoriamente prima di poter richiedere update su transitions activity.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            val task = ActivityRecognition.getClient(requireContext()).requestActivityTransitionUpdates(request, myPendingIntentActivityRecognition)

            task.addOnSuccessListener {
                Log.d("DETECT", "Connesso all'API activity recognition")
            }

            task.addOnFailureListener {
                Log.d("DETECT", "Errore di connessione con l'API activity recognition")
            }

            simulateActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        } else {
            Log.d("WTF", "WTF")
        }
    }

    private fun simulateActivity(activityType: Int, transitionType: Int) {
        // TODO --> PUT EXTRA ALL'INTENT PER DIVERSIFICARE LA TIPOLOGIA DI ACTIVITY RECOGNITION DA CONDURRE.

        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}
