package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.activity.chronometer.RunActivity
import com.example.brockapp.activity.chronometer.WalkActivity
import com.example.brockapp.util.NewUserActivityPermissionUtil
import com.example.brockapp.activity.chronometer.StillActivity
import com.example.brockapp.activity.chronometer.VehicleActivity

import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NewUserActivity: AppCompatActivity() {
    private lateinit var newUserActivityUtil: NewUserActivityPermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_user_activity)
        toolbar.setNavigationIcon(R.drawable.baseline_home_more_24)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<TextView>(R.id.text_view_welcome_new_activity).text =
            "Some activity may require permission to access your location, to improve the use of app"

        val buttonVehicle = findViewById<FloatingActionButton>(R.id.button_vehicle)
        val buttonStill = findViewById<FloatingActionButton>(R.id.button_still)
        val buttonWalk = findViewById<FloatingActionButton>(R.id.button_walk)
        val buttonRun = findViewById<FloatingActionButton>(R.id.button_run)
        setUpButtons(buttonVehicle, buttonRun, buttonStill, buttonWalk)

        newUserActivityUtil = NewUserActivityPermissionUtil(
            this
        ) { enableButtons(buttonVehicle, buttonRun) }

        newUserActivityUtil.requestNewUserActivityPermissions()
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

    // I ust define the other button for run activity
    private fun setUpButtons(buttonVehicle: FloatingActionButton, buttonRun: FloatingActionButton, buttonStill: FloatingActionButton, buttonWalk: FloatingActionButton) {
        buttonVehicle.also {
            it.isEnabled = false
            it.setOnClickListener {
                val intent = Intent(this, VehicleActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        buttonRun.also {
            it.isEnabled = false
            it.setOnClickListener {
                val intent = Intent(this, RunActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        buttonStill.setOnClickListener {
            val intent = Intent(this, StillActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonWalk.also {
            it.setOnClickListener {
                val intent = Intent(this, WalkActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun enableButtons(buttonVehicle: FloatingActionButton, buttonRun: FloatingActionButton) {
        buttonVehicle.isEnabled = true
        buttonRun.isEnabled = true
    }
}