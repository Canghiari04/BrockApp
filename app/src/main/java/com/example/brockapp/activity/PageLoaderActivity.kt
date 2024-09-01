package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.dialog.AccountDialog
import com.example.brockapp.fragment.MapFragment
import com.example.brockapp.fragment.HomeFragment
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.fragment.ChartsFragment
import com.example.brockapp.fragment.FriendsFragment
import com.example.brockapp.fragment.CalendarFragment
import com.example.brockapp.interfaces.InternetAvailableImpl

import android.util.Log
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.content.Context
import android.view.MenuInflater
import kotlinx.coroutines.launch
import android.graphics.PorterDuff
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageLoaderActivity: AppCompatActivity() {
    private var mapFragments = mutableMapOf<String, Fragment>()
    private var internetUtil = InternetAvailableImpl()

    private lateinit var toolbar: Toolbar
    private lateinit var util: NotificationUtil
    private lateinit var mapFragment: MapFragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var manager: NotificationManager
    private lateinit var chartsFragment: ChartsFragment
    private lateinit var friendsFragment: FriendsFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var newActivityButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_loader)

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

        checkIfNetworkIsActive()

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

    private fun checkIfNetworkIsActive() {
        if (!internetUtil.isInternetActive(this)) {
            util = NotificationUtil()
            sendErrorNotification()
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

    private fun sendErrorNotification() {
        manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = util.getConnectivityPendingIntent(this)
        val notification = util.getConnectivityNotification(CHANNEL_ID_CONNECTIVITY_NOTIFY, pendingIntent, this)

        getNotificationChannel()

        manager.notify(ID_CONNECTIVITY_NOTIFY, notification.build())
    }

    private fun getNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_CONNECTIVITY_NOTIFY,
            NAME_CHANNEL_CONNECTIVITY_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_CONNECTIVITY_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }
}