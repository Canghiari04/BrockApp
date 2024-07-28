package com.example.brockapp.detect

import com.example.brockapp.User
import com.example.brockapp.database.DbHelper

import android.util.Log
import java.time.Instant
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
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
            dbHelper.insertUserActivity(activityType.toString(), transitionType.toString(), timestamp, user.id)
        } catch (e: Exception) {
            Log.d("INSERT DATABASE", e.toString())
        }
    }
}