package com.example.brockapp.detect

import com.example.brockapp.User
import com.example.brockapp.database.DbHelper

import android.util.Log
import java.time.Instant
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import com.google.android.gms.location.DetectedActivity
import java.time.format.DateTimeFormatter

class UserActivityBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val user = User.getInstance()
        val dbHelper = DbHelper(context)

        // TODO -> CONTROLLO SULLE ACTITIVITY DI INTERESSE

        val activityType = intent.getIntExtra("activityType", -1)
        val transitionType = intent.getIntExtra("transitionType", -1)
        val timestamp = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        try {

            when (activityType) {
                DetectedActivity.WALKING -> {
                    val stepNumber = intent.getLongExtra("stepNumber", -1)
                    dbHelper.insertUserWalkActivity(user.id, transitionType, timestamp, stepNumber)
                }
                DetectedActivity.IN_VEHICLE -> {
                    val distanceTravelled = intent.getDoubleExtra("distanceTravelled", -1.0)
                    dbHelper.insertUserVehicleActivity(user.id, transitionType, timestamp, distanceTravelled)
                }
            }

        } catch (e: Exception) {
            Log.d("INSERT DATABASE", e.toString())
        }
    }
}