package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class StillActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.still_activity)

        val chronometer = findViewById<Chronometer>(R.id.chronometer)

        val pauseOffset: Long = 0

        setButtonListeners(false, chronometer, pauseOffset)

        setChronometerListener(chronometer)

        findViewById<Button>(R.id.button_start).isEnabled = true
        findViewById<Button>(R.id.button_stop).isEnabled = false
    }

    private fun setChronometerListener(chronometer: Chronometer) {
        var notificationSent = false
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000 ).toInt()
            if (hours == 10 && !notificationSent) {
                sendLazyUserNotification("Torna in attività!", "Sei fermo da più di un'ora ")
                notificationSent = true
            }
        }
    }

    private fun setButtonListeners(
        running: Boolean,
        chronometer: Chronometer,
        pauseOffset: Long,
    ) {
        var running1 = running
        var pauseOffset1 = pauseOffset
        findViewById<Button>(R.id.button_start).setOnClickListener {
            if (!running1) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset1
                chronometer.start()
                running1 = true

                findViewById<Button>(R.id.button_start).isEnabled = false
                findViewById<Button>(R.id.button_stop).isEnabled = true

                registerTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            }

        }

        findViewById<Button>(R.id.button_stop).setOnClickListener {
            if (running1) {
                chronometer.stop()
                pauseOffset1 = SystemClock.elapsedRealtime() - chronometer.base
                running1 = false

                findViewById<Button>(R.id.button_start).isEnabled = true
                findViewById<Button>(R.id.button_stop).isEnabled = false

                chronometer.base = SystemClock.elapsedRealtime()

                registerTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            }
        }
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

    private fun registerTransition(activityType: Int, transitionType: Int) {
        val intent = Intent().apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendLazyUserNotification(title : String, content : String) {
        val intent = Intent(NOTIFICATION_SERVICE)
            .putExtra("title", title)
            .putExtra("content", content)
            .putExtra("type", "walk")

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}