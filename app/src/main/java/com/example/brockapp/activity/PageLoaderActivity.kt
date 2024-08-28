package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.dialog.AccountDialog
import com.example.brockapp.fragment.MapFragment
import com.example.brockapp.fragment.HomeFragment
import com.example.brockapp.fragment.ChartsFragment
import com.example.brockapp.fragment.FriendsFragment
import com.example.brockapp.fragment.CalendarFragment

import android.util.Log
import android.view.Menu
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import kotlinx.coroutines.launch
import android.view.MenuInflater
import kotlinx.coroutines.Dispatchers
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
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
        setSupportActionBar(toolbar)

        newActivityButton = findViewById(R.id.new_activity_button)

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

        newActivityButton.setOnClickListener {
            val intent = Intent(this, NewUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_nav_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_more_info -> {
                AccountDialog().show(supportFragmentManager, "CUSTOM_ACCOUNT_DIALOG")
                true
            }
            R.id.item_more_logout -> {
                val user = User.getInstance()
                user.logoutUser(user)

                goToAuthenticator()
                true
            }
            R.id.item_more_delete -> {
                showDangerousDialog(User.getInstance())
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * Metodo necessario per rimpiazzare il fragment corrente con quello nuovo.
     */
    private fun switchFragment(name: String, fragment: Fragment) {
        hideButton(name)
        hideAllFragment(supportFragmentManager)

        toolbar.title = name

        supportFragmentManager.beginTransaction().apply {
            show(fragment)
            commit()
        }
    }

    private fun hideButton(name: String) {
        when (name) {
            "Map" -> {
                newActivityButton.hide()
            }

            "Friends" -> {
                newActivityButton.hide()
            }

            else -> {
                newActivityButton.show()
            }
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

    private fun showDangerousDialog(user: User) {
        val db = BrockDB.getInstance(this)
        val userDao = db.UserDao()

        AlertDialog.Builder(this)
            .setTitle(R.string.dangerous_dialog_title)
            .setMessage(R.string.dangerous_dialog_message)
            .setPositiveButton(R.string.dangerous_positive_button) { dialog, _ ->
                dialog.dismiss()
                user.logoutUser(user)
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        userDao.deleteUserById(user.id)
                    } catch (e: Exception) {
                        Log.e("PAGE_LOADER_DANGEROUS_ZONE", e.toString())
                    }
                }
                goToAuthenticator()
            }
            .setNegativeButton(R.string.dangerous_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun goToAuthenticator() {
        val intent = Intent(this, AuthenticatorActivity::class.java)
        startActivity(intent)
        finish()
    }
}