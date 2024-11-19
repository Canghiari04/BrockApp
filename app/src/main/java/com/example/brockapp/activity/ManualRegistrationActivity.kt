package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.activity.chronometer.RunActivity
import com.example.brockapp.activity.chronometer.WalkActivity
import com.example.brockapp.activity.chronometer.StillActivity
import com.example.brockapp.activity.chronometer.VehicleActivity
import com.example.brockapp.util.AccessFineLocationPermissionUtil
import com.example.brockapp.util.ActivityRecognitionPermissionUtil

import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManualRegistrationActivity: AppCompatActivity() {

    private lateinit var permissionRecognitionUtil: ActivityRecognitionPermissionUtil
    private lateinit var permissionAccessLocationRunUtil: AccessFineLocationPermissionUtil
    private lateinit var permissionAccessLocationVehicleUtil: AccessFineLocationPermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_registration)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_settings_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        val buttonVehicle = findViewById<FloatingActionButton>(R.id.button_vehicle)
        val buttonStill = findViewById<FloatingActionButton>(R.id.button_still)
        val buttonWalk = findViewById<FloatingActionButton>(R.id.button_walk)
        val buttonRun = findViewById<FloatingActionButton>(R.id.button_run)
        setUpButtons(buttonVehicle, buttonRun, buttonStill, buttonWalk)

        permissionAccessLocationRunUtil = AccessFineLocationPermissionUtil(
            this,
            "Run"
        ) { startRegisterActivity("Run") }

        permissionAccessLocationVehicleUtil = AccessFineLocationPermissionUtil(
            this,
            "Vehicle"
        ) { startRegisterActivity("Vehicle") }

        permissionRecognitionUtil = ActivityRecognitionPermissionUtil(
            this,
            { startRegisterActivity("Walk") },
            null
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
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

    private fun setUpButtons(
        buttonVehicle: FloatingActionButton,
        buttonRun: FloatingActionButton,
        buttonStill: FloatingActionButton,
        buttonWalk: FloatingActionButton
    ) {
        buttonVehicle.apply {
            setOnClickListener {
                permissionAccessLocationRunUtil.requestAccessLocationPermission()
            }
        }

        buttonRun.apply {
            setOnClickListener {
                permissionAccessLocationVehicleUtil.requestAccessLocationPermission()
            }
        }

        buttonStill.setOnClickListener {
            startRegisterActivity("Still")
        }

        buttonWalk.apply {
            setOnClickListener {
                permissionRecognitionUtil.requestActivityRecognitionPermission()
            }
        }
    }

    private fun startRegisterActivity(activity: String) {
        val intent = when (activity) {
            "Vehicle" -> {
                Intent(this, VehicleActivity::class.java)
            }

            "Run" -> {
                Intent(this, RunActivity::class.java)
            }

            "Still" -> {
                Intent(this, StillActivity::class.java)
            }

            else -> {
                Intent(this, WalkActivity::class.java)
            }
        }

        startActivity(intent)
    }
}