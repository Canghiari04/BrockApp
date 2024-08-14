package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE

import android.Manifest
import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.widget.Chronometer
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityRecognition
import com.example.brockapp.manager.ActivityRecognitionManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class StillActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.still_activity)

        val transitionManager = ActivityRecognitionManager()
        val chronometer = findViewById<Chronometer>(R.id.chronometer)

        val pauseOffset: Long = 0

        setButtonListeners(false, chronometer, pauseOffset, transitionManager)

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
        transitionManager: ActivityRecognitionManager
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

            startDetection(transitionManager)
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

    private fun startDetection(transitionManager: ActivityRecognitionManager) {
        val request = transitionManager.getRequest()
        val myPendingIntentActivityRecognition = transitionManager.getPendingIntent(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            val task = ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request, myPendingIntentActivityRecognition)

            task.addOnSuccessListener {
                Log.d("DETECT", "Connesso all'API activity recognition")
            }

            task.addOnFailureListener {
                Log.d("DETECT", "Errore di connessione con l'API activity recognition")
            }

            registerTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        } else {
            Log.d("WTF", "WTF")
        }
    }

    private fun registerTransition(activityType: Int, transitionType: Int) {
        val intent = Intent().apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
        }

        sendBroadcast(intent)
    }

    private fun sendLazyUserNotification(title : String, content : String) {
        val intent = Intent(NOTIFICATION_SERVICE)
            .putExtra("title", title)
            .putExtra("content", content)
            .putExtra("type", "walk")


        //activity?.sendBroadcast(intent)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}