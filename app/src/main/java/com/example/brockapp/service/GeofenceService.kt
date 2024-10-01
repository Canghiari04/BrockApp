package com.example.brockapp.service

import com.example.brockapp.`object`.MyUser
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.GeofenceTransitionEntity

import android.util.Log
import android.os.IBinder
import android.app.Service
import android.content.Intent
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

class GeofenceService: Service() {
    private lateinit var db: BrockDB

    override fun onCreate() {
        super.onCreate()
        db = BrockDB.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val nameLocation = intent.getStringExtra("LOCATION_NAME")
            val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
            val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
            val arrivalTime = intent.getLongExtra("ARRIVAL_TIME", -1L)
            val exitTime = intent.getLongExtra("EXIT_TIME", -1L)

            // Check condition verify that an intent is a dwell transition or an exit transition
            if (exitTime == -1L) {
                val transition = GeofenceTransitionEntity(
                    userId = MyUser.id,
                    nameLocation = nameLocation!!,
                    latitude = latitude,
                    longitude = longitude,
                    arrivalTime = arrivalTime,
                    exitTime = 0
                )

                CoroutineScope(Dispatchers.IO).launch {
                    db.GeofenceTransitionDao().insertGeofenceTransition(transition)
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val lastId = db.GeofenceTransitionDao().getLastInsertedId()!!
                    db.GeofenceTransitionDao().updateExitTime(lastId, exitTime)
                }
            }
        } else {
            Log.d("GEOFENCE_SERVICE", "Transition intent is null")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}