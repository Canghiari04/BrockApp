package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityRecognitionWorker

import android.os.Bundle
import androidx.work.Data
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.widget.TextView
import androidx.work.WorkManager
import android.widget.Chronometer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity

abstract class ChronometerActivity: AppCompatActivity(), NotificationSender {
    private var chronometerIsActive: Boolean = false

    private lateinit var stopButton: Button
    private lateinit var startButton: Button

    protected lateinit var chronometer: Chronometer
    protected lateinit var textViewTypeActivity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chronometer)

        stopButton = findViewById(R.id.button_stop)
        chronometer = findViewById(R.id.chronometer)
        startButton = findViewById(R.id.button_start)
        textViewTypeActivity = findViewById(R.id.text_view_type_activity)

        setUpButtons()
        setUpChronometer()
        setTypeActivityTextView()

        startButton.isEnabled = true
        stopButton.isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (chronometerIsActive) {
                    updateActivity()
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

    private fun setUpButtons() {
        startButton.setOnClickListener {
            if (!chronometerIsActive) {
                chronometer.start()
                chronometer.base = SystemClock.elapsedRealtime()

                chronometerIsActive = true

                stopButton.isEnabled = true
                startButton.isEnabled = false

                registerActivity()
            }
        }

        stopButton.setOnClickListener {
            if (chronometerIsActive) {
                chronometer.stop()

                chronometerIsActive = false

                stopButton.isEnabled = false
                startButton.isEnabled = true

                updateActivity()
            }
        }
    }

    protected abstract fun registerActivity()

    protected abstract fun updateActivity()

    protected abstract fun setUpChronometer()

    protected abstract fun setTypeActivityTextView()
}