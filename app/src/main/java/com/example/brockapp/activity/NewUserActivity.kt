package com.example.brockapp.activity

import com.example.brockapp.R

import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NewUserActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_user_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.baseline_home_more_24)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<FloatingActionButton>(R.id.button_still).setOnClickListener {
            val intent = Intent(this, StillActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<FloatingActionButton>(R.id.button_vehicle).setOnClickListener {
            val intent = Intent(this, VehicleActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<FloatingActionButton>(R.id.button_walk).setOnClickListener {
            val intent = Intent(this, WalkActivity::class.java)
            startActivity(intent)
            finish()

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home")
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
}