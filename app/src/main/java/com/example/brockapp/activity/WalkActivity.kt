package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.NOTIFICATION_INTENT_TYPE
import com.example.brockapp.ACTIVITY_RECOGNITION_NOTIFY
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE

import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.os.SystemClock
import android.content.Intent
import android.hardware.Sensor
import android.content.Context
import android.widget.TextView
import android.widget.Chronometer
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition

class WalkActivity : AppCompatActivity() {
    private var stepCount = 0
    private var running = false
    private var initialStepCount = 0
    private var stepDetectorSensor: Sensor? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walk_activity)

        notificationManager = NotificationManagerCompat.from(this)

        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        val chronometer = findViewById<Chronometer>(R.id.walk_chronometer)

        findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                chronometer.start()
                running = true

                findViewById<Button>(R.id.walk_button_start).isEnabled = false
                findViewById<Button>(R.id.walk_button_stop).isEnabled = true

                startStepCounting()
            }
        }

        findViewById<Button>(R.id.walk_button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                findViewById<Button>(R.id.walk_button_start).isEnabled = true
                findViewById<Button>(R.id.walk_button_stop).isEnabled = false

                stopStepCounting()
                chronometer.setBase(SystemClock.elapsedRealtime())

                val totalSteps = stepCount - initialStepCount
                registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT, totalSteps.toLong())
            }
        }

        var notificationSent = false

        chronometer.setOnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base

            val elapsedSeconds = elapsedMillis / 1000 / 60 / 60

            if (elapsedSeconds >= 1 && !notificationSent) {
                sendWalkNotification("Bravo!", "Stai camminando da più di un'ora!")
                notificationSent = true
            }

        }

        findViewById<Button>(R.id.walk_button_start).isEnabled = true
        findViewById<Button>(R.id.walk_button_stop).isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    private fun startStepCounting() {
        stepDetectorSensor?.also { stepSensor ->
            // sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            initialStepCount = stepCount
        }
    }

    private fun stopStepCounting() {
        // sensorManager.unregisterListener(this)
    }

    fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            Log.d("StepCount", "Passi: $stepCount")

            val stepsDuringSession = stepCount - initialStepCount

            if (stepsDuringSession == 100) {
                sendWalkNotification("Bravo!", "Hai fatto 100 passi!")
            }

            findViewById<TextView>(R.id.step_count)?.text = stepsDuringSession.toString()
        }
    }

    private fun sendWalkNotification(title: String, content: String) {
        val intent = Intent().apply {
            setAction(NOTIFICATION_INTENT_TYPE)
            putExtra("title", title)
            putExtra("content", content)
            putExtra("type", "WALK")
            putExtra("typeNotify", ACTIVITY_RECOGNITION_NOTIFY)
        }

        sendBroadcast(intent)
    }

    private fun registerActivity(activityType: Int, transitionType: Int, stepCount: Long) {
        val intent = Intent().apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("stepNumber", stepCount)
        }

        sendBroadcast(intent)
    }
}