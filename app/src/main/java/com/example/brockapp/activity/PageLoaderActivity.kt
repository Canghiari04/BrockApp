package com.example.brockapp.activity

import android.Manifest
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.fragment.HomeFragment
import com.example.brockapp.fragment.ChartsFragment
import com.example.brockapp.fragment.FriendsFragment
import com.example.brockapp.fragment.CalendarFragment
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.receiver.ConnectivityReceiver

import android.util.Log
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.example.brockapp.viewmodel.GeofenceViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class PageLoaderActivity: AppCompatActivity() {
    private var mapFragments = mutableMapOf<String, Fragment>()

    private lateinit var geofence: MyGeofence
    private lateinit var view: GeofenceViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var homeFragment: HomeFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var chartsFragment: ChartsFragment
    private lateinit var friendsFragment: FriendsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        startBackgroundOperations()

        homeFragment = HomeFragment()
        calendarFragment = CalendarFragment()
        chartsFragment = ChartsFragment()
        friendsFragment = FriendsFragment()

        toolbar = findViewById(R.id.toolbar_page_loader)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.page_loader_fragment, homeFragment)
            add(R.id.page_loader_fragment, calendarFragment)
            add(R.id.page_loader_fragment, chartsFragment)
            add(R.id.page_loader_fragment, friendsFragment)
            commit()
        }

        mapFragments.apply {
            put("Home", homeFragment)
            put("Calendar", calendarFragment)
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
                R.id.navbar_item_plus -> {
                    startActivity(Intent(this, NewUserActivity::class.java))
                    finish()
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
    }

    private fun startBackgroundOperations() {
        val db = BrockDB.getInstance(this)
        val factoryViewModelGeofence = GeofenceViewModelFactory(db)

        view = ViewModelProvider(this, factoryViewModelGeofence)[GeofenceViewModel::class.java]
        view.getGeofenceAreas()

        observeGeofenceAreas()
    }

    private fun observeGeofenceAreas() {
        view.observeGeofenceAreasLiveData().observe(this) {
            geofence = MyGeofence.getInstance()
            geofence.init(this, it)

            startGeofence()
            startConnectivity()
        }
    }

    private fun startGeofence() {
        val geofencingClient = LocationServices.getGeofencingClient(this)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofence.request, geofence.pendingIntent).run {
                addOnSuccessListener {
                    Log.d("GEOFENCING_RECEIVER", "Successful connection.")
                }
                addOnFailureListener {
                    Log.e("GEOFENCING_RECEIVER", "Unsuccessful connection.")
                }
            }
        } else {
            Log.d("WTF", "WTF")
        }
    }

    private fun startConnectivity() {
        val receiver = ConnectivityReceiver()

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
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