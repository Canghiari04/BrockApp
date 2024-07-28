package com.example.brockapp.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.brockapp.database.DbHelper
import com.google.android.gms.location.ActivityTransitionResult


class UserActivityBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dbHelper = DbHelper(context)

        val sharedPreferences = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("userId", -1)

        Log.d("UserId", userId.toString())
    }
}
