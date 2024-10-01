package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.fragment.MapFragment
import com.example.brockapp.fragment.YouFragment
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.fragment.FriendsFragment
import com.example.brockapp.fragment.CalendarFragment
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.singleton.MyActivityRecognition
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import android.Manifest
import android.util.Log
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.app.PendingIntent
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
import com.google.android.gms.location.GeofencingRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageLoaderActivity: AppCompatActivity() {
    private val networkUtil = InternetAvailableImpl()
    private var mapFragments = mutableMapOf<String, Fragment>()

    private lateinit var toolbar: Toolbar
    private lateinit var mapFragment: MapFragment
    private lateinit var youFragment: YouFragment
    private lateinit var receiver: ConnectivityReceiver
    private lateinit var friendsFragment: FriendsFragment
    private lateinit var calendarFragment: CalendarFragment
    private lateinit var viewModelGeofence: GeofenceViewModel
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var newActivityButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_loader)

        // Connectivity service start always, regardless of the user's preferences
        checkConnectivity()
        startConnectivity()

        checkServicesActive()

        toolbar = findViewById(R.id.toolbar_page_loader)

        youFragment = YouFragment()
        calendarFragment = CalendarFragment()
        mapFragment = MapFragment()
        friendsFragment = FriendsFragment()

        settingsButton = findViewById(R.id.button_settings)
        newActivityButton = findViewById(R.id.button_new_activity)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.page_loader_fragment, youFragment)
            add(R.id.page_loader_fragment, calendarFragment)
            add(R.id.page_loader_fragment, mapFragment)
            add(R.id.page_loader_fragment, friendsFragment)
            commit()
        }

        mapFragments.apply {
            put("You", youFragment)
            put("Calendar", calendarFragment)
            put("Map", mapFragment)
            put("Friends", friendsFragment)
        }

        if(intent.hasExtra("FRAGMENT_TO_SHOW")) {
            val name = intent.getStringExtra("FRAGMENT_TO_SHOW")
            switchFragment(name!!, mapFragments[name]!!)
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navbar_item_you -> {
                    switchFragment("You", youFragment)
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

                R.id.navbar_item_friends -> {
                    switchFragment("Friends", friendsFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        newActivityButton.setOnClickListener {
            val intent = Intent(this, NewUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        MyActivityRecognition.removeTask(this)
    }

    private fun checkConnectivity() {
        if (networkUtil.isInternetActive(this)) {
            MyNetwork.isConnected = true
        } else {
            MyNetwork.isConnected = false
            Toast.makeText(this, "You are offline", Toast.LENGTH_LONG).show()
        }
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

    private fun checkServicesActive() {
        val activityRecognition = MySharedPreferences.checkService("ACTIVITY_RECOGNITION",this)

        if (activityRecognition) {
            startActivityRecognition()
        } else {
            Toast.makeText(
                this,
                "Please, check the activity recognition service to utilize all the features of the app",
                Toast.LENGTH_LONG
            ).show()
        }

        val geofenceTransition = MySharedPreferences.checkService("GEOFENCE_TRANSITION",this)

        if (geofenceTransition) {
            startGeofenceTransition()
        } else {
            Toast.makeText(
                this,
                "Please, check the geofence transition service to utilize all the features of the app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startActivityRecognition() {
        val task = MyActivityRecognition.getTask(this)

        task.run {
            addOnSuccessListener {
                Log.d("PAGE_LOADER_ACTIVITY", "Correct implementation")
            }
            addOnFailureListener {
                Log.d("PAGE_LOADER_ACTIVITY", "Bad implementation")
            }
        }
    }

    private fun startGeofenceTransition() {
        val db = BrockDB.getInstance(this)
        val factoryViewModelGeofence = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryViewModelGeofence)[GeofenceViewModel::class.java]

        observeGeofenceAreas()

        viewModelGeofence.fetchGeofenceAreas()
    }

    private fun observeGeofenceAreas() {
        viewModelGeofence.staticAreas.observe(this) {
            if (it.isNotEmpty()) {
                MyGeofence.defineAreas(it)
                MyGeofence.defineRadius(this)

                val request = MyGeofence.getRequest()
                val pendingIntent = MyGeofence.getPendingIntent(this)

                startGeofence(request, pendingIntent)
            } else {
                viewModelGeofence.insertStaticGeofenceAreas()
            }
        }
    }

    private fun startGeofence(request: GeofencingRequest, pendingIntent: PendingIntent) {
        val geofencingClient = LocationServices.getGeofencingClient(this)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(request, pendingIntent).run {
                addOnSuccessListener {
                    Log.d("PAGE_LOADER_ACTIVITY", "Successful connection")
                }
                addOnFailureListener {
                    Log.e("PAGE_LOADER_ACTIVITY", "Unsuccessful connection")
                }
            }
        } else {
            Log.wtf("PAGE_LOADER_ACTIVITY", "Permission denied")
        }
    }

    private fun switchFragment(name: String, fragment: Fragment) {
        hideButton(name)
        hideAllFragment(supportFragmentManager)

        toolbar.title = name

        supportFragmentManager.beginTransaction().apply {
            show(fragment)
            commit()
        }
    }

    private fun hideButton(item: String) {
        if (item == "Calendar") {
            newActivityButton.hide()
        } else {
            newActivityButton.show()
        }
    }

    private fun hideAllFragment(manager: FragmentManager) {
        manager.beginTransaction().apply {
            mapFragments.forEach { (key, value) ->
                hide(value)
            }

            commit()
        }
    }
}