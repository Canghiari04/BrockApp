package com.example.brockapp.activity

import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.example.brockapp.R
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.GEOFENCE_INTENT_TYPE
import com.example.brockapp.manager.GeofenceManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GeofenceTransition

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val intent = Intent().apply {
            action = GEOFENCE_INTENT_TYPE
            putExtra("EXTRA_GEOFENCE_TRANSITION", Geofence.GEOFENCE_TRANSITION_ENTER, )

            putExtra(
                "EXTRA_LOCATION",
                Location("mockProvider").apply {
                    longitude = 44.4827194
                    latitude = 11.3498368
                    accuracy = 10f
                }
            )
        }

        sendBroadcast(intent)
    }
}