package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.receiver.ActivityRecognitionReceiver

import android.os.Bundle
import androidx.work.Data
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import androidx.work.WorkManager
import android.widget.Chronometer
import android.content.IntentFilter
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class StillActivity: AppCompatActivity() {
    private var running: Boolean = false
    private var receiver: ActivityRecognitionReceiver = ActivityRecognitionReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_still)

        supportActionBar?.title = " "

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT_TYPE))

        val chronometer = findViewById<Chronometer>(R.id.still_chronometer)

        setButtonListeners(chronometer)
        setChronometerListener(chronometer)

        findViewById<Button>(R.id.button_start).isEnabled = true
        findViewById<Button>(R.id.button_stop).isEnabled = false
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

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun setButtonListeners(chronometer: Chronometer, ) {
        findViewById<Button>(R.id.button_start).setOnClickListener {
            if (!running) {
                chronometer.start()
                running = true

                findViewById<Button>(R.id.button_start).isEnabled = false
                findViewById<Button>(R.id.button_stop).isEnabled = true

                registerTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            }
        }

        findViewById<Button>(R.id.button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                findViewById<Button>(R.id.button_start).isEnabled = true
                findViewById<Button>(R.id.button_stop).isEnabled = false

                chronometer.base = SystemClock.elapsedRealtime()

                registerTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            }
        }
    }

    private fun setChronometerListener(chronometer: Chronometer) {
        var notificationSent = false

        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000 * 60 * 60).toInt()
            if (hours == 1 && !notificationSent) {
                sendLazyUserNotification("Torna in attività!", "Sei fermo da più di un'ora ")
                notificationSent = true
            }
        }
    }

    private fun registerTransition(activityType: Int, transitionType: Int) {
        val intent = Intent().apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendLazyUserNotification(title: String, content: String) {
        val inputData = Data.Builder()
            .putString("type", 3.toString())
            .putString("title", title)
            .putString("text", content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ActivityRecognitionWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}