package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R

import android.Manifest
import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.os.SystemClock
import android.content.Intent
import android.hardware.Sensor
import android.widget.TextView
import android.widget.Chronometer
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class WalkActivity: AppCompatActivity(), SensorEventListener {
    private var stepCount = 0
    private var running = false
    private var initialStepCount = 0
    private var stepDetectorSensor : Sensor? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walk_activity)

        notificationManager = NotificationManagerCompat.from(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION)
        }

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepDetectorSensor == null) {
            findViewById<Button>(R.id.walk_button_start).isEnabled = false
            findViewById<TextView>(R.id.step_count)?.text = "Sensore non disponibile"

            Log.e("WalkActivity", "Sensore TYPE_STEP_DETECTOR non disponibile sul dispositivo.")

            return
        }

        val chronometer = findViewById<Chronometer>(R.id.walk_chronometer)

        findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                chronometer.start()

                running = true

                findViewById<Button>(R.id.walk_button_start).isEnabled = false
                findViewById<Button>(R.id.walk_button_stop).isEnabled = true

                startStepCounting()

                registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER, 0L)
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
                notificationSent = true
            }

        }

        findViewById<Button>(R.id.walk_button_start).isEnabled = true
        findViewById<Button>(R.id.walk_button_stop).isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, NewUserActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            Log.d("StepCount", "Passi: $stepCount")

            val stepsDuringSession = stepCount - initialStepCount

            if (stepsDuringSession == 100) {

            }

            findViewById<TextView>(R.id.step_count)?.text = stepsDuringSession.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    private fun startStepCounting() {
        stepDetectorSensor.also { stepSensor ->
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            initialStepCount = stepCount
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    private fun registerActivity(activityType: Int, transitionType: Int, stepCount: Long) {
        val intent = Intent().apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("stepNumber", stepCount)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}