package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.interfaces.NotificationSender
import com.example.brockapp.worker.ActivityRecognitionWorker
import com.example.brockapp.service.ActivityRecognitionService

import android.Manifest
import android.os.Bundle
import androidx.work.Data
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.os.SystemClock
import android.widget.TextView
import android.location.Location
import android.widget.Chronometer
import androidx.work.WorkManager
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.work.OneTimeWorkRequestBuilder
import com.google.android.gms.location.Priority
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.FusedLocationProviderClient

class VehicleActivity: AppCompatActivity(), NotificationSender {
    private var running = false
    private var totalDistance = 0.0
    private var startLocation: Location? = null

    private lateinit var distanceTraveled: TextView
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle)
        supportActionBar?.title = " "

        distanceTraveled = findViewById(R.id.vehicle_distance_travelled)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val chronometer = findViewById<Chronometer>(R.id.vehicle_chronometer)
        val vehicleButtonStart = findViewById<Button>(R.id.vehicle_button_start)
        val vehicleButtonStop = findViewById<Button>(R.id.vehicle_button_stop)

        setOnClickListeners(chronometer, vehicleButtonStart, vehicleButtonStop)

        setupLocationUpdates()

        vehicleButtonStart.isEnabled = true
        vehicleButtonStop.isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (running) {
                    registerActivity(
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT,
                        totalDistance
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

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun setOnClickListeners(chronometer: Chronometer, vehicleButtonStart: Button, vehicleButtonStop: Button) {
        vehicleButtonStart.setOnClickListener {
            if (!running) {
                running = true

                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()

                vehicleButtonStart.isEnabled = false
                vehicleButtonStop.isEnabled = true

                startLocationUpdates()
            }

            sendNotification(
                "Buon viaggio!",
                "Ricorda di prestare la massima attenzione alla guida, evita di usare " +
                "il dispositivo"
            )

            registerActivity(
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                0.0
            )
        }

        vehicleButtonStop.setOnClickListener {
            if (running) {
                running = false

                chronometer.stop()

                vehicleButtonStart.isEnabled = true
                vehicleButtonStop.isEnabled = false

                stopLocationUpdates()

                registerActivity(
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT,
                    totalDistance
                )
            }
        }
    }

    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        )
        .setMinUpdateIntervalMillis(POSITION_UPDATE_INTERVAL_MILLIS.toLong())
        .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val locations = locationResult.locations

                if (locations.isNotEmpty()) {
                    val newLocation = locations.last()

                    if (startLocation == null) {
                        startLocation = newLocation
                    } else {
                        startLocation?.let {
                            totalDistance += it.distanceTo(newLocation).toDouble()

                            if (totalDistance < 1000) {
                                val distance = totalDistance.toInt()
                                distanceTraveled.text = String.format("$distance metri")
                            } else {
                                val distanceKilometers = (totalDistance / 1000).toInt()
                                distanceTraveled.text = String.format("$distanceKilometers km")
                            }
                        }

                        startLocation = newLocation
                    }
                }
            }
        }
    }

    private fun registerActivity(transitionType: Int, distanceTraveled: Double) {
        val intent = Intent(this, ActivityRecognitionService::class.java).apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("ACTIVITY_TYPE", DetectedActivity.IN_VEHICLE)
            putExtra("TRANSITION_TYPE", transitionType)
            putExtra("DISTANCE_TRAVELED", distanceTraveled)
        }

        startService(intent)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}