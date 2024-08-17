package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.fragment.HomeFragment
import com.example.brockapp.fragment.ChartsFragment
import com.example.brockapp.fragment.FriendsFragment
import com.example.brockapp.fragment.CalendarFragment

import android.os.Bundle
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PageLoaderActivity : AppCompatActivity() {
    private lateinit var homeFragment: HomeFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var chartsFragment: ChartsFragment
    private lateinit var friendsFragment: FriendsFragment

    private var mapFragments = mutableMapOf<String, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        homeFragment = HomeFragment()
        calendarFragment = CalendarFragment()
        chartsFragment = ChartsFragment()
        friendsFragment = FriendsFragment()

        supportFragmentManager.beginTransaction().apply {
            add(R.id.page_loader_fragment, homeFragment)
            add(R.id.page_loader_fragment, calendarFragment)
            add(R.id.page_loader_fragment, chartsFragment)
            add(R.id.page_loader_fragment, friendsFragment)
            commit()
        }

        mapFragments.apply {
            put("home", homeFragment)
            put("calendar", calendarFragment)
            put("charts", chartsFragment)
            put("friends", friendsFragment)
        }

        if(intent.hasExtra("FRAGMENT_TO_SHOW")) {
            val typeFragment = intent.getStringExtra("FRAGMENT_TO_SHOW")
            switchFragment(mapFragments[typeFragment]!!)
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navbar_item_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.navbar_item_calendar -> {
                    switchFragment(calendarFragment)
                    true
                }
                R.id.navbar_item_plus -> {
                    startActivity(Intent(this, NewUserActivity::class.java))
                    finish()
                    true
                }
                R.id.navbar_item_charts -> {
                    switchFragment(chartsFragment)
                    true
                }
                R.id.navbar_item_friends -> {
                    switchFragment(friendsFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    /**
     * Metodo necessario per rimpiazzare il fragment corrente con quello nuovo.
     */
    private fun switchFragment(fragment: Fragment) {
        hideAllFragment(supportFragmentManager)

        supportFragmentManager.beginTransaction().apply {
            show(fragment)
            commit()
        }
    }

    /**
     * Metodo utilizzato per nascondere tutti i fragment contenuti nel manager.
     */
    private fun hideAllFragment(manager: FragmentManager) {
        manager.beginTransaction().apply {
            mapFragments.forEach { (key, value) ->
                hide(value)
            }
            commit()
        }
    }
}