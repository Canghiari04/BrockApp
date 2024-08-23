package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.fragment.*

import android.os.Bundle
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageLoaderActivity: AppCompatActivity() {
    private var mapFragments = mutableMapOf<String, Fragment>()

    private lateinit var toolbar: Toolbar
    private lateinit var homeFragment: HomeFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var mapFragment: MapFragment
    private lateinit var chartsFragment: ChartsFragment
    private lateinit var friendsFragment: FriendsFragment

    private lateinit var newActivityButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        homeFragment = HomeFragment()
        calendarFragment = CalendarFragment()
        mapFragment = MapFragment()
        chartsFragment = ChartsFragment()
        friendsFragment = FriendsFragment()

        toolbar = findViewById(R.id.toolbar_page_loader)

        newActivityButton = findViewById<FloatingActionButton>(R.id.new_activity_button)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.page_loader_fragment, homeFragment)
            add(R.id.page_loader_fragment, calendarFragment)
            add(R.id.page_loader_fragment, mapFragment)
            add(R.id.page_loader_fragment, chartsFragment)
            add(R.id.page_loader_fragment, friendsFragment)
            commit()
        }

        mapFragments.apply {
            put("Home", homeFragment)
            put("Calendar", calendarFragment)
            put("Map", mapFragment)
            put("Charts", chartsFragment)
            put("Friends", friendsFragment)
        }

        if(intent.hasExtra("FRAGMENT_TO_SHOW")) {
            val name = intent.getStringExtra("FRAGMENT_TO_SHOW")
            switchFragment(name!!, mapFragments[name]!!)
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navbar_item_home -> {
                    switchFragment("Home", homeFragment)
                    true
                }
                R.id.navbar_item_calendar -> {
                    switchFragment("Calendar", calendarFragment)
                    true
                }
                R.id.navbar_item_map -> {
                    switchFragment("Map", mapFragment)
                    true
                }
                R.id.navbar_item_charts -> {
                    switchFragment("Charts", chartsFragment)
                    true
                }
                R.id.navbar_item_friends -> {
                    switchFragment("Friends", friendsFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }

        findViewById<FloatingActionButton>(R.id.new_activity_button).setOnClickListener {
            startActivity(Intent(this, NewUserActivity::class.java))
        }
    }

    /**
     * Metodo necessario per rimpiazzare il fragment corrente con quello nuovo.
     */
    private fun switchFragment(name: String, fragment: Fragment) {
        hideAllFragment(supportFragmentManager)

        toolbar.title = name

        supportFragmentManager.beginTransaction().apply {
            show(fragment)
            commit()
        }

        if (name == "Map")
            newActivityButton.hide()
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