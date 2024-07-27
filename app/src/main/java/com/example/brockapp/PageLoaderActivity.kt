package com.example.brockapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.activity.HomeActivity
import com.example.brockapp.activity.NewUserActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PageLoaderActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        /*
         * Dall'intent acquisisco la tipologia di fragment che dovrà essere sovrapposta.
         */
        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navbar_item_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navbar_item_plus -> {
                    startActivity(Intent(this, NewUserActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}