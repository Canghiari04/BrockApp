package com.example.brockapp.service

import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.worker.GeofenceWorker
import com.example.brockapp.database.GeofenceTransitionEntity

import java.util.Locale
import android.os.IBinder
import androidx.work.Data
import android.app.Service
import android.content.Intent
import android.location.Geocoder
import kotlinx.coroutines.launch
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import androidx.work.OneTimeWorkRequestBuilder

class GeofenceService: Service() {
    private lateinit var db: BrockDB

    override fun onCreate() {
        super.onCreate()
        db = BrockDB.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.INSERT.toString() -> {
                val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
                val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
                val arrivalTime = intent.getLongExtra("ARRIVAL_TIME", 0L)

                val nameLocation = getLocationName(latitude, longitude)

                notify(nameLocation)
                insert(nameLocation, latitude, longitude, arrivalTime)
            }

            Actions.UPDATE.toString() -> {
                val exitTime = intent.getLongExtra("EXIT_TIME", 0L)
                update(exitTime)
            }

            Actions.STOP.toString() -> {
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    enum class Actions {
        INSERT, UPDATE, STOP
    }

    private fun notify(item: String) {
        val inputData = Data.Builder().putString("LOCATION_NAME", item).build()

        val request = OneTimeWorkRequestBuilder<GeofenceWorker>().setInputData(inputData).build()
        WorkManager.getInstance(this).enqueue(request)
    }

    private fun insert(nameLocation: String, latitude: Double, longitude: Double, arrivalTime: Long) {
        val transition = GeofenceTransitionEntity(
            userId = MyUser.id,
            nameLocation = nameLocation,
            latitude = latitude,
            longitude = longitude,
            arrivalTime = arrivalTime,
            exitTime = 0L
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.GeofenceTransitionDao().insertGeofenceTransition(transition)
        }
    }

    private fun getLocationName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 10)

        if (!addresses.isNullOrEmpty()) {
            var locationName = " "

            for (address in addresses) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Check if the feature name found by the geocoder is already equal to the name inside the db
                    if (db.GeofenceAreaDao().countGeofenceAreaName(address.featureName)) {
                        locationName = address.featureName
                        return@launch
                    }
                }

                continue
            }

            return locationName
        }

        return " "
    }

    private fun update(exitTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val lastId = db.GeofenceTransitionDao().getLastInsertedId()!!
            db.GeofenceTransitionDao().updateExitTime(lastId, exitTime)
        }
    }
}