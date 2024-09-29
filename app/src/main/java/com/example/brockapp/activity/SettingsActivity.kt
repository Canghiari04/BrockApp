package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.permission.ActivityRecognitionPermission
import com.example.brockapp.permission.GeofenceTransitionPermissions

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.content.Context
import androidx.annotation.RequiresApi
import android.content.SharedPreferences
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity: AppCompatActivity() {
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var switchDumpDatabase: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchGeofenceTransition: SwitchCompat
    private lateinit var switchActivityRecognition: SwitchCompat
    private lateinit var geofenceUtil: GeofenceTransitionPermissions
    private lateinit var recognitionUtil: ActivityRecognitionPermission

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        sharedPreferences = this.getSharedPreferences(
            "${User.id}_${User.username}_${User.password}",
            Context.MODE_PRIVATE
        )

        editor = sharedPreferences.edit()

        switchDumpDatabase = findViewById(R.id.switch_share_dump_database)
        switchGeofenceTransition = findViewById(R.id.switch_geofence_transition_service)
        switchActivityRecognition = findViewById(R.id.switch_activity_recognition_service)

        // Creating the launcher for the permissions required by the app
        geofenceUtil = GeofenceTransitionPermissions(
            this,
            { changeCheckSwitch("GEOFENCE_TRANSITION", switchGeofenceTransition) },
            switchGeofenceTransition
        )

        recognitionUtil = ActivityRecognitionPermission(
            this,
            { changeCheckSwitch("ACTIVITY_RECOGNITION", switchActivityRecognition) },
            switchActivityRecognition
        )
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

    override fun onResume() {
        super.onResume()

        // Set up switches in base of the user's shared preferences
        setUpSwitchDumpDatabase()
        setUpSwitchGeofenceTransition()
        setUpSwitchActivityRecognition()
    }

    // Callback provided to the permission's launcher
    private fun changeCheckSwitch(key: String, switch: SwitchCompat) {
        editor.putBoolean(key, true).apply()

        switch.isChecked = true
        switch.trackTintList = ContextCompat.getColorStateList(baseContext, R.color.uni_red)
    }

    private fun setUpSwitchDumpDatabase() {
        switchDumpDatabase.run {
            isChecked = sharedPreferences.getBoolean("DUMP_DATABASE", false)

            trackTintList = if (isChecked) {
                ContextCompat.getColorStateList(context, R.color.uni_red)
            } else {
                ContextCompat.getColorStateList(context, R.color.grey)
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    editor.putBoolean("DUMP_DATABASE", true)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.uni_red)
                } else {
                    editor.putBoolean("DUMP_DATABASE", false)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)
                }

                editor.apply()
            }
        }
    }

    private fun setUpSwitchGeofenceTransition() {
        switchGeofenceTransition.run {
            isChecked = sharedPreferences.getBoolean("GEOFENCE_TRANSITION", false)

            trackTintList = if (isChecked) {
                ContextCompat.getColorStateList(context, R.color.uni_red)
            } else {
                ContextCompat.getColorStateList(context, R.color.grey)
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    geofenceUtil.requestGeofenceTransitionPermissions()
                } else {
                    editor.putBoolean("GEOFENCE_TRANSITION", false)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)
                }

                editor.apply()
            }
        }
    }

    private fun setUpSwitchActivityRecognition() {
        switchActivityRecognition.run {
            isChecked = sharedPreferences.getBoolean("ACTIVITY_RECOGNITION", false)

            trackTintList = if (isChecked) {
                ContextCompat.getColorStateList(context, R.color.uni_red)
            } else {
                ContextCompat.getColorStateList(context, R.color.grey)
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    recognitionUtil.requestActivityRecognitionPermission()
                } else {
                    editor.putBoolean("ACTIVITY_RECOGNITION", false)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)
                }

                editor.apply()
            }
        }
    }
}