package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil

import android.Manifest
import android.os.Binder
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.location.Location
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

class DistanceService: Service() {

    private var binder = LocalBinder()
    private var distance: Double = 0.0
    private var startLocation: Location? = null
    private var notificationUtil = NotificationUtil()

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    inner class LocalBinder: Binder() {
        fun getService(): DistanceService = this@DistanceService
    }

    override fun onCreate() {
        super.onCreate()

        setUpLocationUpdates()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        start()
    }

    override fun onBind(intent: Intent?): IBinder {
        startMonitoring()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopMonitoring()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun resetDistance() {
        distance = 0.0
    }

    fun getDistance(): Double {
        return distance
    }

    private fun start() {
        startForeground(
            ID_DISTANCE_SERVICE_NOTIFY,
            notificationUtil.getNotificationBody(
                CHANNEL_ID_DISTANCE_SERVICE,
                R.drawable.icon_run,
                "BrockApp - Distance tracking",
                "Brock App is tracking your distance travelled in background",
                this
            ).build()
        )
    }

    private fun setUpLocationUpdates() {
        locationRequest = LocationRequest
            .Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000)
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
                            distance += it.distanceTo(newLocation).toDouble()
                        }

                        startLocation = newLocation
                    }
                }
            }
        }
    }

    private fun startMonitoring() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun stopMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopSelf()
    }
}