package com.example.brockapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.brockapp.fragment.GraphsFragment
import com.example.brockapp.fragment.HomeFragment
import com.example.brockapp.fragment.NewActivityFragment
import com.example.brockapp.fragment.OptionsFragment
import com.example.brockapp.fragment.PlusFragment
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
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navbar_item_graphics -> {
                    replaceFragment(GraphsFragment())
                    true
                }
                R.id.navbar_item_plus -> {
                    replaceFragment(PlusFragment())
                    true
                }
                R.id.navbar_item_activities -> {
                    replaceFragment(NewActivityFragment())
                    true
                }
                R.id.navbar_item_more -> {
                    replaceFragment(OptionsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.page_loader_fragment, fragment)
            .commit()
    }
}