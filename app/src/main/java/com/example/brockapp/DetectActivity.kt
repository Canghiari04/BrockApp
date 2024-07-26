package com.example.brockapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.detect.UserActivityBroadcastReceiver
import com.google.android.gms.location.DetectedActivity

class DetectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detect_activity)
        sendTestBroadcast(this)
    }

    fun sendTestBroadcast(context: Context) {
        val intent = Intent(context, UserActivityBroadcastReceiver::class.java)
        intent.putParcelableArrayListExtra("activity_data", arrayListOf(
            DetectedActivity(DetectedActivity.WALKING, 75),
            DetectedActivity(DetectedActivity.RUNNING, 50)
        ))
        context.sendBroadcast(intent)
    }
}