package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.service.ActivityRecognitionService

import android.os.Bundle
import androidx.work.Data
import android.widget.Toast
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.content.Context
import android.hardware.Sensor
import android.widget.TextView
import androidx.work.WorkManager
import android.widget.Chronometer
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition

class WalkActivity: AppCompatActivity(), SensorEventListener, NotificationSender {
    private var running = false
    private var initialStepCount = 0
    private var sessionStepCount = 0
    private var stepCounterSensor: Sensor? = null
    private var notWalkingNotificationSent: Boolean = false
    private var hourSpentWalkingNotification: Boolean = false
    private var lastStepTime: Long = System.currentTimeMillis()

    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)

        supportActionBar?.title = " "

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val chronometer = findViewById<Chronometer>(R.id.walk_chronometer)
        val walkStartButton = findViewById<Button>(R.id.walk_button_start)
        val walkStopButton = findViewById<Button>(R.id.walk_button_stop)

        setUpOnClickListeners(chronometer, walkStartButton, walkStopButton)

        walkStartButton.isEnabled = true
        walkStopButton.isEnabled = false
    }

    private fun setUpOnClickListeners(
        chronometer: Chronometer,
        walkStartButton: Button,
        walkStopButton: Button
    ) {
        walkStartButton.setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()

                sessionStepCount = 0
                running = true

                walkStartButton.isEnabled = false
                walkStopButton.isEnabled = true

                startStepCounting()
                Toast.makeText(
                    this,
                    "Iniziato conteggio passi",
                    Toast.LENGTH_SHORT
                ).show()

                hourSpentWalkingNotification = false
                notWalkingNotificationSent = false

                registerActivity(
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                    0L
                )
            }
        }

        walkStopButton.setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                walkStartButton.isEnabled = true
                walkStopButton.isEnabled = false

                stopStepCounting()

                registerActivity(
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT,
                    sessionStepCount.toLong()
                )
            }
        }

        chronometer.setOnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
            val elapsedHour = elapsedMillis / 1000 / 60 / 60

            if (elapsedHour >= 1 && !hourSpentWalkingNotification) {
                sendNotification(
                    "Continua così!",
                    "Stai camminando da più di un'ora!"
                )
                hourSpentWalkingNotification = true
            }

            if (System.currentTimeMillis() - lastStepTime >= NOT_WALKING_NOTIFICATION_TIME_MILLIS && !notWalkingNotificationSent){
                sendNotification(
                    "Ricomincia a camminare!",
                    "Non stai facendo passi da troppo tempo!"
                )
                notWalkingNotificationSent = true
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (initialStepCount < 0) {
            initialStepCount = event.values[0].toInt()
        }

        sessionStepCount = event.values[0].toInt() - initialStepCount
        findViewById<TextView>(R.id.step_count)?.text = sessionStepCount.toString()

        lastStepTime = System.currentTimeMillis()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    override fun sendNotification(title: String, content: String) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("text", content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ActivityRecognitionWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
        notWalkingNotificationSent = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (running) {
                    registerActivity(
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT,
                        sessionStepCount.toLong()
                    )
                }

                val intent = Intent(this, NewUserActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun startStepCounting() {
        initialStepCount = -1
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
    }

    // Richiamo un service dedito alla registrazione su DB.
    private fun registerActivity(transitionType: Int, stepCount: Long) {
        val intent = Intent(this, ActivityRecognitionService::class.java).apply {
            action = ACTIVITY_RECOGNITION_INTENT_TYPE
            putExtra("ACTIVITY_TYPE", DetectedActivity.WALKING)
            putExtra("TRANSITION_TYPE", transitionType)
            putExtra("STEP_NUMBER", stepCount)
        }

        startService(intent)
    }
}