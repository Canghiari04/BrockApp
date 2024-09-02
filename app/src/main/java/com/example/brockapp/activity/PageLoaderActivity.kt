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
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.interfaces.NetworkAvailableImpl

import android.util.Log
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import android.view.MenuItem
import android.content.Intent
import android.view.MenuInflater
import kotlinx.coroutines.launch
import android.graphics.PorterDuff
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import android.net.ConnectivityManager
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageLoaderActivity: AppCompatActivity() {
    private var internetUtil = NetworkAvailableImpl()
    private var mapFragments = mutableMapOf<String, Fragment>()

    private lateinit var toolbar: Toolbar
    private lateinit var mapFragment: MapFragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var receiver: ConnectivityReceiver
    private lateinit var chartsFragment: ChartsFragment
    private lateinit var friendsFragment: FriendsFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var newActivityButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_loader)

        startConnectivity()
        checkIfNetworkIsActive()

        toolbar = findViewById(R.id.toolbar_page_loader)
        setSupportActionBar(toolbar)
        toolbar.overflowIcon?.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)

        homeFragment = HomeFragment()
        calendarFragment = CalendarFragment()
        mapFragment = MapFragment()
        chartsFragment = ChartsFragment()
        friendsFragment = FriendsFragment()

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
            put("Calendario", calendarFragment)
            put("Mappa", mapFragment)
            put("Grafici", chartsFragment)
            put("Amici", friendsFragment)
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
                    switchFragment("Calendario", calendarFragment)
                    true
                }
                R.id.navbar_item_map -> {
                    switchFragment("Mappa", mapFragment)
                    true
                }
                R.id.navbar_item_charts -> {
                    switchFragment("Grafici", chartsFragment)
                    true
                }
                R.id.navbar_item_friends -> {
                    switchFragment("Amici", friendsFragment)
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun startConnectivity() {
        receiver = ConnectivityReceiver(this)

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun checkIfNetworkIsActive() {
        if (!internetUtil.isInternetActive(this)) {
            Toast.makeText(this, "Connessione non rilevata. Alcune funzionalità saranno disabilitate", Toast.LENGTH_LONG).show()
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
            "Mappa" -> {
                newActivityButton.hide()
            }

            "Amici" -> {
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