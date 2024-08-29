package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.receiver.ActivityRecognitionReceiver

import android.Manifest
import android.util.Log
import android.os.Bundle
import android.widget.Toast
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.hardware.Sensor
import android.content.Context
import android.widget.TextView
import android.widget.Chronometer
import android.content.IntentFilter
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

class WalkActivity : AppCompatActivity(), SensorEventListener {
    private var stepCount = 0
    private var running = false
    private var heightDifference = 0f

    private var currentSteps = 0

    private var stepDetectorSensor : Sensor? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManagerCompat

    private var pressureSensor: Sensor? = null
    private var initialAltitude: Float? = null

    private var receiver : ActivityRecognitionReceiver = ActivityRecognitionReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walk_activity)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT_TYPE))

        notificationManager = NotificationManagerCompat.from(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "Sensore barometrico non disponibile", Toast.LENGTH_SHORT).show()
        }


        if (stepDetectorSensor == null) {
            Log.e("WalkActivity", "Sensore TYPE_STEP_DETECTOR non disponibile sul dispositivo.")

            findViewById<TextView>(R.id.step_count)?.text = "Sensore non disponibile"
            findViewById<Button>(R.id.walk_button_start).isEnabled = false

            Log.e("WalkActivity", "Sensore TYPE_STEP_DETECTOR non disponibile sul dispositivo.")

            return
        } else {
            Toast.makeText(this, "Il sensore non è null", Toast.LENGTH_SHORT).show()
        }

        val chronometer = findViewById<Chronometer>(R.id.walk_chronometer)

        findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime()
                heightDifference = 0f
                chronometer.start()
                running = true

                findViewById<Button>(R.id.walk_button_start).isEnabled = false
                findViewById<Button>(R.id.walk_button_stop).isEnabled = true

                startStepCounting()
                Toast.makeText(this, "Iniziato conteggio passi", Toast.LENGTH_SHORT).show()

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

                registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT, currentSteps.toLong())
            }
        }

        var notificationSent = false

        chronometer.setOnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base

            val elapsedHour = elapsedMillis / 1000 / 60 / 60

            if (elapsedHour >= 1 && !notificationSent) {
                notificationSent = true
                // Deve richiamare il worker per Activity Recognition
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startStepCounting() {
        stepCount = 0
        stepDetectorSensor.also { stepSensor ->
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Toast.makeText(this, "Sensore registrato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                stepCount++
                Log.d("StepCount", "Passi: $stepCount")

                val stepsDuringSession = stepCount

                if (stepsDuringSession == 100) {
                    // Deve richiamare il worker per Activity Recognition
                }

                currentSteps = stepCount
                findViewById<TextView>(R.id.step_count)?.text = currentSteps.toString()
            }
            Sensor.TYPE_PRESSURE -> {
                val pressure = event.values[0]
                val currentAltitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)

                if (initialAltitude == null) {
                    initialAltitude = currentAltitude
                } else {
                    val altitudeDifference = Math.abs(currentAltitude - initialAltitude!!)
                    heightDifference += altitudeDifference
                }

                // Aggiorna la UI con il dislivello
                findViewById<TextView>(R.id.height_difference_count)?.text = "${heightDifference} metri"
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


    private fun registerActivity(activityType: Int, transitionType: Int, stepCount: Long, heightDifference: Float? = null) {
        val intent = Intent().apply {
            action = ACTIVITY_RECOGNITION_INTENT_TYPE
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("stepNumber", stepCount)
            putExtra("heightDifference", heightDifference)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun unregisterActivityRecognitionReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        unregisterActivityRecognitionReceiver()
        super.onDestroy()
    }
}
