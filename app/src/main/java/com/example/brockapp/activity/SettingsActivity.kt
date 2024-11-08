package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.ScheduleWorkerImpl
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.singleton.MyActivityRecognition
import com.example.brockapp.service.ActivityRecognitionService
import com.example.brockapp.util.ActivityRecognitionPermissionUtil
import com.example.brockapp.util.GeofenceTransitionPermissionsUtil

import java.io.File
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.DetectedActivity

class SettingsActivity: AppCompatActivity() {
    private var switchMapper = mapOf<SwitchCompat, Pair<String, Int>>()

    private lateinit var viewModel: UserViewModel
    private lateinit var switchDumpDatabase: SwitchCompat
    private lateinit var scheduleWorkerUtil: ScheduleWorkerImpl
    private lateinit var switchGeofenceTransition: SwitchCompat
    private lateinit var switchActivityRecognition: SwitchCompat
    private lateinit var geofenceUtil: GeofenceTransitionPermissionsUtil
    private lateinit var recognitionUtil: ActivityRecognitionPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Toolbar>(R.id.toolbar_settings_activity).also {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setSupportActionBar(it)
            it.setTitle(R.string.text_blank)
        }

        findViewById<TextView>(R.id.text_view_description_dump_database).text =
            "Sharing your data allows the app to provide a personalized experience!"

        findViewById<TextView>(R.id.text_view_description_activities).text =
            "Choose the activities that you want to detect"

        switchDumpDatabase = findViewById(R.id.switch_share_dump_database)
        switchGeofenceTransition = findViewById(R.id.switch_geofence_transition_service)
        switchActivityRecognition = findViewById(R.id.switch_activity_recognition_service)

        scheduleWorkerUtil = ScheduleWorkerImpl(this)

        switchMapper = mapOf(
            findViewById<SwitchCompat>(R.id.switch_vehicle_activity) to Pair("VEHICLE_ACTIVITY", DetectedActivity.IN_VEHICLE),
            findViewById<SwitchCompat>(R.id.switch_run_activity) to Pair("RUN_ACTIVITY", DetectedActivity.RUNNING),
            findViewById<SwitchCompat>(R.id.switch_still_activity) to Pair("STILL_ACTIVITY", DetectedActivity.STILL),
            findViewById<SwitchCompat>(R.id.switch_walk_activity) to Pair("WALK_ACTIVITY", DetectedActivity.WALKING)
        )

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val factoryViewModel = UserViewModelFactory(db, s3Client, file)
        viewModel = ViewModelProvider(this, factoryViewModel)[UserViewModel::class.java]

        setUpSwitchActivities()
        setUpSwitchDumpDatabase()

        observeRecordingOnS3()

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

                        Intent(context, ActivityRecognitionService::class.java).also {
                            it.action = ActivityRecognitionService.Actions.RESTART.toString()

                            if (MyActivityRecognition.getStatus()) {
                                startService(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun SwitchCompat.colorStateList() = if (isChecked) {
        ContextCompat.getColorStateList(context, R.color.uni_red)
    } else {
        ContextCompat.getColorStateList(context, R.color.grey)
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
                    viewModel.registerUserToS3(MyUser.username)
                    trackTintList = ContextCompat.getColorStateList(context, R.color.uni_red)

                    MySharedPreferences.setService("DUMP_DATABASE", true, context)
                } else {
                    scheduleWorkerUtil.deleteSyncPeriodic()
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)

                    MySharedPreferences.setService("DUMP_DATABASE", false, context)
                }
            }
        }
    }

    private fun observeRecordingOnS3() {
        viewModel.recording.observe(this) {
            if (it) {
                scheduleWorkerUtil.scheduleSyncPeriodic()
            }
        }
    }

    private fun setUpSwitchGeofenceTransition() {
        switchGeofenceTransition.run {
            isChecked = checkGeofenceService()

            trackTintList = colorStateList()

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // The action to set true the switch is allowed iif the permission is not denied
                    geofenceUtil.requestGeofenceTransitionPermissions()
                } else {
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)

                    Intent(context, GeofenceService::class.java).also {
                        it.action = GeofenceService.Actions.TERMINATE.toString()

                        if (MyGeofence.getStatus()) {
                            startService(it)
                        }
                    }

                    MySharedPreferences.setService("GEOFENCE_TRANSITION", false, context)
                    MyGeofence.setStatus(false)
                }
            }
        }
    }

    private fun checkGeofenceService(): Boolean {
        return MySharedPreferences.checkService("GEOFENCE_TRANSITION", this) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpSwitchActivityRecognition() {
        switchActivityRecognition.run {
            isChecked = checkActivityRecognitionService()

            trackTintList = colorStateList()

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    recognitionUtil.requestActivityRecognitionPermission()
                } else {
                    trackTintList = ContextCompat.getColorStateList(context, R.color.grey)

                    Intent(context, ActivityRecognitionService::class.java).also {
                        it.action = ActivityRecognitionService.Actions.TERMINATE.toString()

                        if (MyActivityRecognition.getStatus()) {
                            startService(it)
                        }
                    }

                    MySharedPreferences.setService("ACTIVITY_RECOGNITION", false, context)
                }
            }
        }
    }

    private fun checkActivityRecognitionService(): Boolean {
        return MySharedPreferences.checkService("ACTIVITY_RECOGNITION", this) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }
}