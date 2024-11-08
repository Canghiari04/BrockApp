package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.worker.GeofenceWorker
import com.example.brockapp.room.GeofenceTransitionsEntity
import com.example.brockapp.interfaces.ReverseGeocodingImpl

import android.Manifest
import android.util.Log
import java.time.Instant
import android.os.IBinder
import androidx.work.Data
import android.app.Service
import java.time.ZoneOffset
import android.content.Intent
import kotlinx.coroutines.async
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import java.time.format.DateTimeFormatter
import androidx.work.OneTimeWorkRequestBuilder
import com.google.android.gms.location.LocationServices

class GeofenceService: Service() {
    private lateinit var db: BrockDB
    private lateinit var geocodingUtil: ReverseGeocodingImpl

    override fun onCreate() {
        super.onCreate()

        db = BrockDB.getInstance(this)
        geocodingUtil = ReverseGeocodingImpl(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.INSERT.toString() -> {
                val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
                val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
                val arrivalTime = intent.getLongExtra("ARRIVAL_TIME", 0L)

                CoroutineScope(Dispatchers.IO).launch {
                    var featureName: String? = null

                    val job = async {
                        featureName = geocodingUtil.getGeofenceName(latitude, longitude)
                    }

                    job.await()
                    notify(featureName)
                    insert(featureName, latitude, longitude, arrivalTime)
                }
            }

            Actions.UPDATE.toString() -> {
                val exitTime = intent.getLongExtra("EXIT_TIME", 0L)
                update(exitTime)
            }

            Actions.RESTART.toString() -> {
                CoroutineScope(Dispatchers.IO).launch {
                    restartMonitoring()
                    stopSelf()
                }
            }

            Actions.TERMINATE.toString() -> {
                terminateMonitoring()
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    enum class Actions {
        INSERT, UPDATE, RESTART, TERMINATE
    }

    private fun notify(item: String?) {
        val inputData = Data.Builder().putString("LOCATION_NAME", item ?: "Unknown").build()

        OneTimeWorkRequestBuilder<GeofenceWorker>().setInputData(inputData).build().also {
            WorkManager.getInstance(this).enqueue(it)
        }
    }

    private fun insert(nameLocation: String?, latitude: Double, longitude: Double, arrivalTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val transition = GeofenceTransitionsEntity(
                username = MyUser.username,
                timestamp = getInstant(),
                nameLocation = nameLocation ?: " ",
                longitude = longitude,
                latitude = latitude,
                arrivalTime = arrivalTime,
                exitTime = 0L
            )

            val job = async {
                db.GeofenceTransitionsDao().insertGeofenceTransition(transition)
            }

            job.await()
            stopSelf()
        }
    }

    private fun getInstant(): String {
        return DateTimeFormatter
            .ofPattern(ISO_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }

    private fun update(exitTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val job = async {
                val lastId = db.GeofenceTransitionsDao().getLastInsertedId()
                db.GeofenceTransitionsDao().updateExitTime(lastId, exitTime)
            }

            job.await()
            stopSelf()
        }
    }

    private suspend fun restartMonitoring() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
        }

        MyGeofence.defineAreas(db.GeofenceAreasDao().getGeofenceAreasByUsername(MyUser.username))
        MyGeofence.defineRadius(this)

        val geofenceClient = LocationServices.getGeofencingClient(this)
        val pendingIntent = MyGeofence.getPendingIntent(this)
        val request = MyGeofence.getRequest()

        geofenceClient.removeGeofences(pendingIntent).run {
            addOnSuccessListener {
                Log.d("GEOFENCE_SERVICE", "Geofence removed")
            }
        }

        geofenceClient.addGeofences(request, pendingIntent).run {
            addOnSuccessListener {
                Log.d("GEOFENCE_SERVICE", "Geofence added")
            }
        }
    }

    private fun terminateMonitoring() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
        }

        val geofenceClient = LocationServices.getGeofencingClient(this)
        val pendingIntent = MyGeofence.getPendingIntent(this)

        geofenceClient.removeGeofences(pendingIntent).run {
            addOnSuccessListener {
                Log.d("CONNECTIVITY_SERVICE", "Geofence removed")
            }
            addOnFailureListener {
                Log.e("CONNECTIVITY_SERVICE", "Geofence not removed")
            }
        }
    }
}