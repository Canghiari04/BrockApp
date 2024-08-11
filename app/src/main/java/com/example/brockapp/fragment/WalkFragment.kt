package com.example.brockapp.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.R
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

class WalkFragment : Fragment(R.layout.walk_fragment), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
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
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val chronometer = view.findViewById<Chronometer>(R.id.walk_chronometer)
        var pauseOffset: Long = 0

        view.findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                //chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()

                running = true

                view.findViewById<Button>(R.id.walk_button_start).isEnabled = false
                view.findViewById<Button>(R.id.walk_button_stop).isEnabled = true

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

                stopStepCounting()
                chronometer.setBase(SystemClock.elapsedRealtime())

                val totalSteps = stepCount - initialStepCount
                registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT, totalSteps.toLong())
            }
        }

            //esempio di notifica che viene mandata dopo 30 secondi di camminata
            chronometer.setOnChronometerTickListener {
                val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                val seconds = (elapsedTime / 1000).toInt()
                if (seconds == 30) {
                    sendWalkNotification(context)
                }
        }

        view.findViewById<Button>(R.id.walk_button_start).isEnabled = true
        view.findViewById<Button>(R.id.walk_button_stop).isEnabled = false
    }

    private fun sendWalkNotification(context: Context) {
        val channelId = "1"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_directions_walk_24)
            .setContentTitle("Bravo!")
            .setContentText("Stai camminando da più di 30 secondi!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val intent = Intent("NOTIFICATION").apply {
            putExtra("notification", notification)
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun startStepCounting() {
        stepCounterSensor?.also { stepSensor ->
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            initialStepCount = stepCount
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
            Log.d("StepCount", "Passi: $stepCount")

            val stepsDuringSession = stepCount - initialStepCount
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
