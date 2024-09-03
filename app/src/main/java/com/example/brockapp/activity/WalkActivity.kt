package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.receiver.ActivityRecognitionReceiver

import android.os.Bundle
import androidx.work.Data
import android.widget.Toast
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.hardware.Sensor
import android.content.Context
import android.widget.TextView
import androidx.work.WorkManager
import android.widget.Chronometer
import android.content.IntentFilter
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class WalkActivity: AppCompatActivity(), SensorEventListener {
    private var running = false
    private var initialStepCount = 0
    private var sessionStepCount = 0
    private var heightDifference = 0f
    private var pressureSensor: Sensor? = null
    private var initialAltitude: Float? = null
    private var stepCounterSensor: Sensor? = null
    private var receiver: ActivityRecognitionReceiver = ActivityRecognitionReceiver()

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)

        supportActionBar?.title = " "

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT_TYPE))

        notificationManager = NotificationManagerCompat.from(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "Sensore barometrico non disponibile", Toast.LENGTH_SHORT).show()
        }

        if (stepCounterSensor == null) {
            findViewById<Button>(R.id.walk_button_start).isEnabled = false
            findViewById<TextView>(R.id.step_count)?.text = "Sensore non disponibile"
            return
        }

        val chronometer = findViewById<Chronometer>(R.id.walk_chronometer)

        findViewById<Button>(R.id.walk_button_start).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
                heightDifference = 0f
                sessionStepCount = 0
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

                registerActivity(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT, sessionStepCount.toLong())
            }
        }

        var hourSpentWalkingNotification = false

        chronometer.setOnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base

            val elapsedHour = elapsedMillis / 1000 / 60 / 60

            if (elapsedHour >= 1 && !hourSpentWalkingNotification) {
                hourSpentWalkingNotification = true

                val inputData = Data.Builder()
                    .putString("type", 7.toString())
                    .putString("title", "Continua così!")
                    .putString("text", "Stai camminando da più di un'ora")
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ActivityRecognitionWorker>()
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(this).enqueue(workRequest)
            }
        }

        findViewById<Button>(R.id.walk_button_start).isEnabled = true
        findViewById<Button>(R.id.walk_button_stop).isEnabled = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                if (initialStepCount < 0) {
                    initialStepCount = event.values[0].toInt()
                }
                sessionStepCount = event.values[0].toInt() - initialStepCount

                findViewById<TextView>(R.id.step_count)?.text = sessionStepCount.toString()
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

                findViewById<TextView>(R.id.height_difference_count)?.text = "${heightDifference} metri"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
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

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun startStepCounting() {
        initialStepCount = -1
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
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
}