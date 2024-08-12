package com.example.brockapp.activity

import android.content.Intent
import android.os.Bundle
import com.example.brockapp.R
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.GEOFENCE_INTENT_TYPE

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val intent = Intent(GEOFENCE_INTENT_TYPE)
        sendBroadcast(intent)
    }
}