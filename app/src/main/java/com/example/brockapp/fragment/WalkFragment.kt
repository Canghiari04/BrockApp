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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.NOTIFICATION_INTENT_FILTER
import com.example.brockapp.R
import com.example.brockapp.notification.NotificationService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

class WalkFragment : Fragment(R.layout.walk_fragment), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var running = false
    private var stepCount = 0
    private var initialStepCount = 0
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assicurati che il contesto sia disponibile
        val context = requireContext()  // Ora sicuro di essere chiamato nel momento giusto
        notificationManager = NotificationManagerCompat.from(context)

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        val chronometer = view.findViewById<Chronometer>(R.id.walk_chronometer)

        view.findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                chronometer.start()
                running = true

                view.findViewById<Button>(R.id.walk_button_start).isEnabled = false
                view.findViewById<Button>(R.id.walk_button_stop).isEnabled = true

                startStepCounting()
            }
        }

        view.findViewById<Button>(R.id.walk_button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                view.findViewById<Button>(R.id.walk_button_start).isEnabled = true
                view.findViewById<Button>(R.id.walk_button_stop).isEnabled = false

                stopStepCounting()
                chronometer.setBase(SystemClock.elapsedRealtime())

                val totalSteps = stepCount - initialStepCount
                registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT, totalSteps.toLong())
            }
        }

        // Esempio di notifica che viene mandata dopo 30 secondi di camminata
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val seconds = (elapsedTime / 1000).toInt()
            if (seconds == 10) {
                sendWalkNotification("Bravo!", "Stai camminando da più di 30 secondi!")
            }
        }

        view.findViewById<Button>(R.id.walk_button_start).isEnabled = true
        view.findViewById<Button>(R.id.walk_button_stop).isEnabled = false
    }

    private fun sendWalkNotification(title: String, content: String) {
        val intent = Intent(NOTIFICATION_INTENT_FILTER)

//        val intent = Intent(requireContext(), NotificationService::class.java).apply {
//            putExtra("title", title)
//            putExtra("content", content)
//            putExtra("type", "walk")
//        }

        activity?.sendBroadcast(intent)
    }


    private fun startStepCounting() {
        stepDetectorSensor?.also { stepSensor ->
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            initialStepCount = stepCount
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            Log.d("StepCount", "Passi: $stepCount")

            val stepsDuringSession = stepCount - initialStepCount

            if (stepsDuringSession == 100) {
                sendWalkNotification("Bravo!", "Hai fatto 100 passi!")
            }
            view?.findViewById<TextView>(R.id.step_count)?.text = stepsDuringSession.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun registerActivity(activityType: Int, transitionType: Int, stepCount: Long) {
        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("stepNumber", stepCount)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}
