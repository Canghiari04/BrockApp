package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.singleton.User

import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.content.SharedPreferences
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity: AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        sharedPreferences = this.getSharedPreferences(
            User.id.toString() + "_" + User.username + "_" + User.password,
            Context.MODE_PRIVATE
        )

        editor = sharedPreferences.edit()

        val switchActivityRecognition = findViewById<SwitchCompat>(R.id.switch_activity_recognition_service)
        val switchGeofenceTransition = findViewById<SwitchCompat>(R.id.switch_geofence_transition_service)
        val switchShareDatabase = findViewById<SwitchCompat>(R.id.switch_share_dump_database)

        setUpSwitch(switchActivityRecognition, switchGeofenceTransition, switchShareDatabase)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra(
                    "FRAGMENT_TO_SHOW",
                    "You"
                )
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    // They can be used only after a change by the user
    private fun setUpSwitch(switchActivityRecognition: SwitchCompat, switchGeofenceTransition: SwitchCompat, switchShareDatabase: SwitchCompat) {
        switchActivityRecognition.isChecked = sharedPreferences.getBoolean("ACTIVITY_RECOGNITION", false)
        switchGeofenceTransition.isChecked = sharedPreferences.getBoolean("GEOFENCE_TRANSITION", false)
        switchShareDatabase.isChecked = sharedPreferences.getBoolean("DUMP_DATABASE", false)

        switchActivityRecognition.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean("ACTIVITY_RECOGNITION", true)
            } else {
                editor.putBoolean("ACTIVITY_RECOGNITION", false)
            }

            editor.apply()
        }

        switchGeofenceTransition.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean("GEOFENCE_TRANSITION", true)
            } else {
                editor.putBoolean("GEOFENCE_TRANSITION", false)
            }

            editor.apply()
        }

        switchShareDatabase.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean("DUMP_DATABASE", true)
            } else {
                editor.putBoolean("DUMP_DATABASE", true)
            }

            editor.apply()
        }
    }
}