package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.POSITION_UPDATE_INTERVAL_MILLIS
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE
import com.example.brockapp.POSITION_UPDATE_INTERVAL_MILLIS
import com.example.brockapp.R
import com.example.brockapp.receiver.ActivityRecognitionReceiver
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class VehicleActivity : AppCompatActivity() {
    private var running = false
    private var totalDistance = 0.0

    private var startLocation: Location? = null

    private lateinit var distanceTravelled: TextView
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var receiver: ActivityRecognitionReceiver = ActivityRecognitionReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vehicle_activity)

        distanceTravelled = findViewById(R.id.vehicle_distance_travelled)

        val chronometer = findViewById<Chronometer>(R.id.chronometer)
        val vehicleButtonStart = findViewById<Button>(R.id.vehicle_button_start)
        val vehicleButtonStop = findViewById<Button>(R.id.vehicle_button_stop)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationUpdates()

        setOnClickListeners(vehicleButtonStart, chronometer, vehicleButtonStop)

        vehicleButtonStart.isEnabled = true
        vehicleButtonStop.isEnabled = false
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

    private fun setOnClickListeners(vehicleButtonStart: Button, chronometer: Chronometer, vehicleButtonStop: Button) {
        vehicleButtonStart.setOnClickListener {
            if (!running) {

                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()

                running = true

                vehicleButtonStart.isEnabled = false
                vehicleButtonStop.isEnabled = true

                startLocationUpdates()
            }

            registerActivity(
                DetectedActivity.IN_VEHICLE,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                0.0
            )
        }

        vehicleButtonStop.setOnClickListener {
            if (running) {
                chronometer.stop()
                running = false

                vehicleButtonStart.isEnabled = true
                vehicleButtonStop.isEnabled = false

                stopLocationUpdates()

                registerActivity(
                    DetectedActivity.IN_VEHICLE,
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT,
                    totalDistance
                )
            }
        }
    }

    /**
     * Costruisce una richiesta di aggiornamento di posizione.Gestisce l'aggiornamento della
     * posizione in background e mostra a schermo la distanza percorsa.
     */
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
                            distanceTravelled.text = String.format("%.2f meters", totalDistance)
                        }

                        startLocation = newLocation
                    }
                }
            }
        }
    }

    /**
     * Controlla se i permessi sono stati garantiti e richiama la funzione per iniziare
     * l'aggiornamento della posizione.
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    /**
     * Ferma l'aggiornamento della posizione.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun registerActivity(activityType: Int, transitionType: Int, distanceTravelled: Double) {
        val intent = Intent().apply {
            setAction(ACTIVITY_RECOGNITION_INTENT_TYPE)
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("distanceTravelled", distanceTravelled)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun unregisterActivityRecognitionReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        unregisterActivityRecognitionReceiver()
        super.onDestroy()
    }

}