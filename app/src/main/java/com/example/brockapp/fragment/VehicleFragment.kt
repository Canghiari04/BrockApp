package com.example.brockapp.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.R
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.Priority

class VehicleFragment : Fragment(R.layout.vehicle_fragment) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var startLocation: Location? = null
    private var totalDistance = 0.0

    private var running = false
    private var pauseOffset: Long = 0

    private lateinit var distanceTravelledText : String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupLocationUpdates()

        distanceTravelledText = view.findViewById<TextView>(R.id.vehicle_distance_travelled).text.toString()

        view.findViewById<Button>(R.id.vehicle_button_start).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true

                view.findViewById<Button>(R.id.vehicle_button_start).isEnabled = false
                view.findViewById<Button>(R.id.vehicle_button_stop).isEnabled = true

                // Start location updates
                startLocationUpdates()
            }
            registerActivity(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER, 0.0)
        }

        view.findViewById<Button>(R.id.vehicle_button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false

                view.findViewById<Button>(R.id.vehicle_button_start).isEnabled = true
                view.findViewById<Button>(R.id.vehicle_button_stop).isEnabled = false

                // Stop location updates
                stopLocationUpdates()

                // Register final distance
                registerActivity(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT, totalDistance)
            }
        }

        view.findViewById<Button>(R.id.vehicle_button_start).isEnabled = true
        view.findViewById<Button>(R.id.vehicle_button_stop).isEnabled = false
    }

    private fun setupLocationUpdates() {
        // Crea una nuova richiesta di aggiornamento della posizione
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000) // Intervallo minimo di aggiornamento
            .build()

        // Configura il callback per gestire i risultati della posizione
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Gestisce i risultati della posizione
                val locations = locationResult.locations
                if (locations.isNotEmpty()) {
                    val newLocation = locations.last() // Ottieni l'ultima posizione
                    if (startLocation == null) {
                        startLocation = newLocation
                    } else {
                        startLocation?.let {
                            totalDistance += it.distanceTo(newLocation).toDouble()
                            distanceTravelledText = totalDistance.toString()
                        }
                        startLocation = newLocation
                    }
                }
            }
        }
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun registerActivity(activityType: Int, transitionType: Int, distanceTravelled: Double) {
        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("distanceTravelled", distanceTravelled)
        }
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}
