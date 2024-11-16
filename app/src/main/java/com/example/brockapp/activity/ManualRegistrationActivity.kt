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

class NewUserActivity: AppCompatActivity() {
    private lateinit var recognitionUtil: ActivityRecognitionPermissionUtil
    private lateinit var accessRunLocationUtil: AccessFineLocationPermissionUtil
    private lateinit var accessVehicleLocationUtil: AccessFineLocationPermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_user_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getText(R.string.toolbar_new_activity)

        val buttonVehicle = findViewById<FloatingActionButton>(R.id.button_vehicle)
        val buttonStill = findViewById<FloatingActionButton>(R.id.button_still)
        val buttonWalk = findViewById<FloatingActionButton>(R.id.button_walk)
        val buttonRun = findViewById<FloatingActionButton>(R.id.button_run)
        setUpButtons(buttonVehicle, buttonRun, buttonStill, buttonWalk)

        accessVehicleLocationUtil = AccessFineLocationPermissionUtil(
            this,
        ) { startRegisterActivity("Vehicle") }

        accessRunLocationUtil = AccessFineLocationPermissionUtil(
            this
        ) { startRegisterActivity("Run") }

        recognitionUtil = ActivityRecognitionPermissionUtil(
            this,
            { startRegisterActivity("Walk") },
            null
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
                }

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
                accessVehicleLocationUtil.requestAccessFineLocation()
            }
        }

        buttonRun.apply {
            setOnClickListener {
                accessRunLocationUtil.requestAccessFineLocation()
            }
        }

        buttonStill.setOnClickListener {
            startRegisterActivity("Still")
        }

        buttonWalk.apply {
            setOnClickListener {
                recognitionUtil.requestActivityRecognitionPermission()
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
        finish()
    }
}