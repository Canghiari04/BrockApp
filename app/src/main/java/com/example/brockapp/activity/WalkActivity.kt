package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.receiver.ActivityRecognitionReceiver

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

class WalkActivity: AppCompatActivity(), SensorEventListener, NotificationSender {
    private var running = false
    private var initialStepCount = 0
    private var sessionStepCount = 0
    private var heightDifference = 0f
    private var stepCounterSensor: Sensor? = null
    private var notWalkingNotificationSent: Boolean = false
    private var hourSpentWalkingNotification: Boolean = false
    private var lastStepTime: Long = System.currentTimeMillis()
    private var receiver: ActivityRecognitionReceiver = ActivityRecognitionReceiver()

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)

        supportActionBar?.title = " "

        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver,
            IntentFilter(ACTIVITY_RECOGNITION_INTENT_TYPE)
        )

        notificationManager = NotificationManagerCompat.from(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        if (stepCounterSensor == null) {
            findViewById<Button>(R.id.walk_button_start).isEnabled = false
            findViewById<TextView>(R.id.step_count)?.text = "Sensore non disponibile"
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
                Toast.makeText(
                    this,
                    "Iniziato conteggio passi",
                    Toast.LENGTH_SHORT
                ).show()

                hourSpentWalkingNotification = false
                notWalkingNotificationSent = false

                registerActivity(
                    DetectedActivity.WALKING,
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                    0L
                )
            }
        }

        findViewById<Button>(R.id.walk_button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                findViewById<Button>(R.id.walk_button_start).isEnabled = true
                findViewById<Button>(R.id.walk_button_stop).isEnabled = false

                stopStepCounting()

                registerActivity(
                    DetectedActivity.WALKING,
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

        findViewById<Button>(R.id.walk_button_start).isEnabled = true
        findViewById<Button>(R.id.walk_button_stop).isEnabled = false
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (running) {
                    registerActivity(
                        DetectedActivity.WALKING,
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

    override fun sendNotification(title: String, content: String) {
        val inputData = Data.Builder()
            .putString("type", 7.toString())
            .putString("title", title)
            .putString("text", content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ActivityRecognitionWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
        notWalkingNotificationSent = true
    }
}