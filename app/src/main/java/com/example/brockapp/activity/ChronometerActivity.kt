package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.os.Bundle
import java.time.Instant
import androidx.work.Data
import java.time.ZoneOffset
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.widget.TableRow
import android.widget.TextView
import androidx.work.WorkManager
import android.widget.Chronometer
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity

abstract class ChronometerActivity: AppCompatActivity(), NotificationSender {
    companion object {
        const val TO_KM = 1000.0
    }

    private var chronometerIsActive: Boolean = false

    private lateinit var stopButton: Button
    private lateinit var startButton: Button
    protected lateinit var chronometer: Chronometer
    protected lateinit var textViewTypeActivity: TextView

    // Sensor tab layout
    protected lateinit var firstTableRow: TableRow
    protected lateinit var secondTableRow: TableRow
    protected lateinit var textViewTitleFirstSensor: TextView
    protected lateinit var textViewValueFirstSensor: TextView
    protected lateinit var textViewTitleSecondSensor: TextView
    protected lateinit var textViewValueSecondSensor: TextView

    protected lateinit var viewModel: ActivitiesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chronometer)

        val db = BrockDB.getInstance(this)
        val viewModelFactory = ActivitiesViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory)[ActivitiesViewModel::class.java]

        stopButton = findViewById(R.id.button_stop)
        startButton = findViewById(R.id.button_start)
        chronometer = findViewById(R.id.chronometer_view)
        textViewTypeActivity = findViewById(R.id.text_view_type_activity)

        firstTableRow = findViewById(R.id.table_row_first_sensor)
        secondTableRow = findViewById(R.id.table_row_second_sensor)
        textViewTitleFirstSensor = findViewById(R.id.text_view_type_first_sensor)
        textViewValueFirstSensor = findViewById(R.id.text_view_value_first_sensor)
        textViewTitleSecondSensor = findViewById(R.id.text_view_type_second_sensor)
        textViewValueSecondSensor = findViewById(R.id.text_view_value_second_sensor)

        setUpButtons()
        setKindOfSensors()
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
                chronometer.base = SystemClock.elapsedRealtime()

                chronometerIsActive = false

                stopButton.isEnabled = false
                startButton.isEnabled = true

                updateActivity()
            }
        }
    }

    protected abstract fun registerActivity()

    protected abstract fun updateActivity()

    protected abstract fun setKindOfSensors()

    protected abstract fun setUpChronometer()

    protected abstract fun setTypeActivityTextView()

    protected fun getInstant(): String {
        return DateTimeFormatter
            .ofPattern(ISO_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }
}