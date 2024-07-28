package com.example.brockapp.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.brockapp.database.DbHelper
import com.google.android.gms.location.ActivityTransitionResult
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class UserActivityBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dbHelper = DbHelper(context)

        val sharedPreferences = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("userId", -1)


        val activityType = intent.getIntExtra("activityType", -1)
        val transitionType = intent.getIntExtra("transitionType", -1)
        val timestamp = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        dbHelper.insertUserActivity(activityType.toString(), transitionType.toString(), timestamp, userId)

        Log.d("UserId", userId.toString())
    }
}
