package com.example.brockapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class PageLoaderActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        /*
         * Dall'intent acquisisco la tipologia di fragment che dovrà essere posta in primo piano.
         */
        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navbar_item_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navbar_item_activities -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.navbar_item_plus -> {
                    startActivity(Intent(this, NewUserActivity::class.java))
                    true
                }
                R.id.navbar_item_charts ->{
                    startActivity(Intent(this, ChartsActivity::class.java))
                    true
                }
                R.id.navbar_item_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}