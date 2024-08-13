package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.fragment.HomeFragment
import com.example.brockapp.fragment.ChartFragment
import com.example.brockapp.fragment.FriendFragment
import com.example.brockapp.fragment.CalendarFragment

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.GEOFENCE_INTENT_TYPE
import com.google.android.material.bottomnavigation.BottomNavigationView

class PageLoaderActivity: AppCompatActivity()  {
    private lateinit var homeFragment: HomeFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var chartFragment: ChartFragment
    private lateinit var friendFragment: FriendFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        homeFragment = HomeFragment()
        calendarFragment = CalendarFragment()
        chartFragment = ChartFragment()
        friendFragment = FriendFragment()

        supportFragmentManager.beginTransaction().apply {
            add(R.id.page_loader_fragment, homeFragment)
            add(R.id.page_loader_fragment, calendarFragment)
            add(R.id.page_loader_fragment, chartFragment)
            add(R.id.page_loader_fragment, friendFragment)
            hide(calendarFragment)
            hide(chartFragment)
            hide(friendFragment)
            commit()
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navbar_item_home -> {
                    supportFragmentManager.beginTransaction().apply {
                        hide(calendarFragment)
                        hide(chartFragment)
                        hide(friendFragment)
                        show(homeFragment)
                        commit()
                    }
                    true
                }
                R.id.navbar_item_calendar -> {
                    supportFragmentManager.beginTransaction().apply {
                        hide(homeFragment)
                        hide(chartFragment)
                        hide(friendFragment)
                        show(calendarFragment)
                        commit()
                    }
                    true
                }
                R.id.navbar_item_plus -> {
                    startActivity(Intent(this, NewUserActivity::class.java))
                    finish()
                    true
                }
                R.id.navbar_item_charts -> {
                    supportFragmentManager.beginTransaction().apply {
                        hide(homeFragment)
                        hide(calendarFragment)
                        hide(friendFragment)
                        show(chartFragment)
                        commit()
                    }
                    true
                }
                R.id.navbar_item_friends -> {
                    supportFragmentManager.beginTransaction().apply {
                        hide(homeFragment)
                        hide(calendarFragment)
                        hide(chartFragment)
                        show(friendFragment)
                        commit()
                    }
                    true
                }
                else ->  {
                    false
                }
            }
        }
    }
}