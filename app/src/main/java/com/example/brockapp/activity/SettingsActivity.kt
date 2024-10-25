package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.worker.SyncBucketWorker
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.singleton.MyActivityRecognition
import com.example.brockapp.service.ActivityRecognitionService
import com.example.brockapp.util.ActivityRecognitionPermissionUtil
import com.example.brockapp.util.GeofenceTransitionPermissionsUtil

import android.util.Log
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import androidx.work.WorkManager
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationServices

class SettingsActivity: AppCompatActivity() {
    private var switchMapper = mapOf<SwitchCompat, Pair<String, Int>>()

    private lateinit var switchDumpDatabase: SwitchCompat
    private lateinit var switchGeofenceTransition: SwitchCompat
    private lateinit var switchActivityRecognition: SwitchCompat
    private lateinit var geofenceUtil: GeofenceTransitionPermissionsUtil
    private lateinit var recognitionUtil: ActivityRecognitionPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        findViewById<TextView>(R.id.text_view_description_dump_database).text =
            "Sharing your data allows the app to provide a personalized experience!"

        findViewById<TextView>(R.id.text_view_description_activities).text =
            "Choose the activities that you want to detect"

        switchDumpDatabase = findViewById(R.id.switch_share_dump_database)
        switchGeofenceTransition = findViewById(R.id.switch_geofence_transition_service)
        switchActivityRecognition = findViewById(R.id.switch_activity_recognition_service)

        switchMapper = mapOf(
            findViewById<SwitchCompat>(R.id.switch_vehicle_activity) to Pair("VEHICLE_ACTIVITY", DetectedActivity.IN_VEHICLE),
            findViewById<SwitchCompat>(R.id.switch_run_activity) to Pair("RUN_ACTIVITY", DetectedActivity.RUNNING),
            findViewById<SwitchCompat>(R.id.switch_still_activity) to Pair("STILL_ACTIVITY", DetectedActivity.STILL),
            findViewById<SwitchCompat>(R.id.switch_walk_activity) to Pair("WALK_ACTIVITY", DetectedActivity.WALKING)
        )

        setUpSwitchActivities()
        setUpSwitchDumpDatabase()

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
                    R.id.navbar_item_you
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
        setUpSwitchGeofenceTransition()
        setUpSwitchActivityRecognition()
    }

    // Callback provided to set true a switch when the permission is allowed
    private fun changeCheckSwitch(key: String, switch: SwitchCompat) {
        MySharedPreferences.setService(key, true, this)

        switch.isChecked = true
        switch.trackTintList = ContextCompat.getColorStateList(baseContext, R.color.uni_red)
    }

    // I used the mapper to retrieve all the data saved inside the shared preferences
    private fun setUpSwitchActivities() {
        val isActive = MySharedPreferences.checkService("ACTIVITY_RECOGNITION", this)

        switchMapper.let {
            it.forEach { (key, value) ->
                key.run {
                    isActivated = isActive
                    isChecked = (MySharedPreferences.getActivity(value.first, context) != DetectedActivity.UNKNOWN)

                    trackTintList = colorStateList()

                    setOnCheckedChangeListener { _, isChecked ->
                        handleSwitchToggle(
                            isChecked,
                            value.first,
                            value.second,
                            key
                        )

                        MyActivityRecognition.setStatus(false)
                    }
                }
            }
        }
    }

    private fun handleSwitchToggle(isChecked: Boolean, key: String, constant: Int, switch: SwitchCompat) {
        if (isChecked) {
            MySharedPreferences.setActivity(key, constant, this)
            switch.trackTintList = ContextCompat.getColorStateList(this, R.color.uni_red)
        } else {
            MySharedPreferences.setActivity(key, DetectedActivity.UNKNOWN, this)
            switch.trackTintList = ContextCompat.getColorStateList(this, R.color.grey)
        }
    }

    private fun setUpSwitchDumpDatabase() {
        switchDumpDatabase.run {
            isChecked = MySharedPreferences.checkService("DUMP_DATABASE", context)

            trackTintList = colorStateList()

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    trackTintList = ContextCompat.getColorStateList(context, R.color.uni_red)

                    MySharedPreferences.setService("DUMP_DATABASE", true, context)
                    OneTimeWorkRequestBuilder<SyncBucketWorker>().build().also {
                        WorkManager.getInstance(context).enqueue(it)
                    }
                } else {
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)

                    MySharedPreferences.setService("DUMP_DATABASE", false, context)
                    WorkManager.getInstance(context).cancelUniqueWork("SyncBucketWorker")
                }
            }
        }
    }

    private fun setUpSwitchGeofenceTransition() {
        switchGeofenceTransition.run {
            isChecked = MySharedPreferences.checkService("GEOFENCE_TRANSITION", context)

            trackTintList = colorStateList()

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // The action to set true the switch is allowed iif the permission is not denied
                    geofenceUtil.requestGeofenceTransitionPermissions()
                } else {
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)

                    MySharedPreferences.setService("GEOFENCE_TRANSITION", false, context)
                    Intent(context, GeofenceService::class.java).also {
                        it.action = GeofenceService.Actions.STOP.toString()
                        startService(it)
                    }

                    val geofenceClient = LocationServices.getGeofencingClient(context)
                    val pendingIntent = MyGeofence.getPendingIntent(context)

                    geofenceClient.removeGeofences(pendingIntent).run {
                        addOnSuccessListener {
                            Log.d("CONNECTIVITY_SERVICE", "Geofence removed")
                        }
                        addOnFailureListener {
                            Log.e("CONNECTIVITY_SERVICE", "Geofence not removed")
                        }
                    }

                    MyGeofence.setStatus(false)
                }
            }
        }
    }

    private fun setUpSwitchActivityRecognition() {
        switchActivityRecognition.run {
            isChecked = MySharedPreferences.checkService("ACTIVITY_RECOGNITION", context)

            trackTintList = colorStateList()

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    recognitionUtil.requestActivityRecognitionPermission()
                } else {
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)

                    MySharedPreferences.setService("ACTIVITY_RECOGNITION", false, context)
                    Intent(context, ActivityRecognitionService::class.java).also {
                        it.action = ActivityRecognitionService.Actions.STOP.toString()
                        startService(it)
                    }

                    MyActivityRecognition.removeTask(context)
                }
            }
        }
    }

    private fun SwitchCompat.colorStateList() = if (isChecked) {
        ContextCompat.getColorStateList(context, R.color.uni_red)
    } else {
        ContextCompat.getColorStateList(context, R.color.grey)
    }
}