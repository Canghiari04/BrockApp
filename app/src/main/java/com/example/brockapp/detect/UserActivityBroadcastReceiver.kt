package com.example.brockapp.detect

import com.example.brockapp.User
import com.example.brockapp.DATE_FORMAT

import android.util.Log
import java.time.Instant
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity
import com.example.brockapp.database.UserWalkActivityEntity
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserActivityBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val user = User.getInstance()
        val db = BrockDB.getInstance(context)

        // TODO -> CONTROLLO SULLE ACTITIVITY DI INTERESSE

        val activityType = intent.getIntExtra("activityType", -1)
        val transitionType = intent.getIntExtra("transitionType", -1)
        val timestamp = DateTimeFormatter
            .ofPattern(DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userStillActivityDao = db.UserStillActivityDao()
                val userVehicleActivityDao = db.UserVehicleActivityDao()
                val userWalkActivityDao = db.UserWalkActivityDao()

                when (activityType) {
                    DetectedActivity.STILL -> {
                        userStillActivityDao.insertStillActivity(UserStillActivityEntity(userId = user.id, transitionType = transitionType, timestamp = timestamp))
                    }
                    DetectedActivity.IN_VEHICLE -> {
                        val distanceTravelled = intent.getDoubleExtra("distanceTravelled", -1.0)
                        userVehicleActivityDao.insertVehicleActivity(UserVehicleActivityEntity(userId = user.id, transitionType = transitionType, timestamp = timestamp, distanceTravelled = distanceTravelled))
                    }
                    DetectedActivity.WALKING -> {
                        val stepNumber = intent.getLongExtra("stepNumber", -1)
                        userWalkActivityDao.insertWalkActivity(UserWalkActivityEntity(userId = user.id, transitionType = transitionType, timestamp = timestamp, stepNumber = stepNumber))
                    }
                }
            } catch (e: Exception) {
                Log.d("INSERT DATABASE", e.toString())
            }
        }
    }
}