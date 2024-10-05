package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.service.SyncDataService
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.util.ActivityRecognitionPermissionUtil
import com.example.brockapp.util.GeofenceTransitionPermissionsUtil

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity: AppCompatActivity() {
    private lateinit var switchDumpDatabase: SwitchCompat
    private lateinit var switchGeofenceTransition: SwitchCompat
    private lateinit var switchActivityRecognition: SwitchCompat
    private lateinit var geofenceUtil: GeofenceTransitionPermissionsUtil
    private lateinit var recognitionUtil: ActivityRecognitionPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        switchDumpDatabase = findViewById(R.id.switch_share_dump_database)
        switchGeofenceTransition = findViewById(R.id.switch_geofence_transition_service)
        switchActivityRecognition = findViewById(R.id.switch_activity_recognition_service)

        // Creating the launcher for the permissions required by the app
        geofenceUtil = GeofenceTransitionPermissionsUtil(
            this,
            { changeCheckSwitch("GEOFENCE_TRANSITION", switchGeofenceTransition) },
            switchGeofenceTransition
        )

        recognitionUtil = ActivityRecognitionPermissionUtil(
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

    // Callback provided to set true a switch when the permission is allowed
    private fun changeCheckSwitch(key: String, switch: SwitchCompat) {
        MySharedPreferences.setService(key, true, this)

        switch.isChecked = true
        switch.trackTintList = ContextCompat.getColorStateList(baseContext, R.color.uni_red)
    }

    private fun setUpSwitchDumpDatabase() {
        switchDumpDatabase.run {
            isChecked = MySharedPreferences.checkService("DUMP_DATABASE", context)

            trackTintList = if (isChecked) {
                ContextCompat.getColorStateList(context, R.color.uni_red)
            } else {
                ContextCompat.getColorStateList(context, R.color.grey)
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    MySharedPreferences.setService("DUMP_DATABASE", true, context)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.uni_red)

                    startService(Intent(context, SyncDataService::class.java))
                } else {
                    MySharedPreferences.setService("DUMP_DATABASE", false, context)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)
                }
            }
        }
    }

    private fun setUpSwitchGeofenceTransition() {
        switchGeofenceTransition.run {
            isChecked = MySharedPreferences.checkService("GEOFENCE_TRANSITION", context)

            trackTintList = if (isChecked) {
                ContextCompat.getColorStateList(context, R.color.uni_red)
            } else {
                ContextCompat.getColorStateList(context, R.color.grey)
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // The action to set true the switch is allowed iif the permission is not denied
                    geofenceUtil.requestGeofenceTransitionPermissions()
                } else {
                    MySharedPreferences.setService("GEOFENCE_TRANSITION", false, context)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)
                }
            }
        }
    }

    private fun setUpSwitchActivityRecognition() {
        switchActivityRecognition.run {
            isChecked = MySharedPreferences.checkService("ACTIVITY_RECOGNITION", context)

            trackTintList = if (isChecked) {
                ContextCompat.getColorStateList(context, R.color.uni_red)
            } else {
                ContextCompat.getColorStateList(context, R.color.grey)
            }

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    recognitionUtil.requestActivityRecognitionPermission()
                } else {
                    MySharedPreferences.setService("ACTIVITY_RECOGNITION", false, context)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)
                }
            }
        }
    }
}