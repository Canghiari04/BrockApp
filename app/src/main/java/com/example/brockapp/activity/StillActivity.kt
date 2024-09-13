package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.service.ActivityRecognitionService

import android.os.Bundle
import androidx.work.Data
import android.view.MenuItem
import android.widget.Button
import android.os.SystemClock
import android.content.Intent
import androidx.work.WorkManager
import android.widget.Chronometer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition

class StillActivity: AppCompatActivity(), NotificationSender {
    private var running: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_still)
        supportActionBar?.title = " "

        val chronometer = findViewById<Chronometer>(R.id.still_chronometer)
        val stillButtonStart = findViewById<Button>(R.id.still_button_start)
        val stillButtonStop = findViewById<Button>(R.id.still_button_stop)

        setOnClickListeners(chronometer, stillButtonStart, stillButtonStop)
        setChronometerListener(chronometer)

        stillButtonStart.isEnabled = true
        stillButtonStop.isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (running) {
                    registerActivity(
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT
                    )
                }

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

    override fun sendNotification(title: String, content: String) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("text", content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ActivityRecognitionWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun setOnClickListeners(chronometer: Chronometer, stillButtonStart: Button, stillButtonStop: Button) {
        stillButtonStart.setOnClickListener {
            if (!running) {
                chronometer.start()
                running = true

                findViewById<Button>(R.id.still_button_start).isEnabled = false
                findViewById<Button>(R.id.still_button_stop).isEnabled = true

                registerActivity(
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER
                )
            }
        }

        stillButtonStop.setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                findViewById<Button>(R.id.still_button_start).isEnabled = true
                findViewById<Button>(R.id.still_button_stop).isEnabled = false

                chronometer.base = SystemClock.elapsedRealtime()

                registerActivity(
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT
                )
            }
        }
    }

    private fun setChronometerListener(chronometer: Chronometer) {
        var notificationSent = false

        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000).toInt()

            if (hours >= 10 && !notificationSent) {
                sendNotification(
                    "Torna in attività!",
                    "Sei fermo da più di un'ora"
                )

                notificationSent = true
            }
        }
    }

    private fun registerActivity(transitionType: Int) {
        val intent = Intent(this, ActivityRecognitionService::class.java).apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("ACTIVITY_TYPE", DetectedActivity.STILL)
            putExtra("TRANSITION_TYPE", transitionType)
        }

        startService(intent)
    }
}