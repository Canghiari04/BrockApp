package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.fragment.MapFragment
import com.example.brockapp.fragment.YouFragment
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.fragment.GroupFragment
import com.example.brockapp.worker.DailyMemoWorker
import com.example.brockapp.viewmodel.MemoViewModel
import com.example.brockapp.fragment.CalendarFragment
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.viewmodel.MemoViewModelFactory
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.singleton.MyActivityRecognition
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.service.ActivityRecognitionService
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import android.Manifest
import android.util.Log
import android.os.Bundle
import java.time.LocalDate
import android.content.Intent
import androidx.work.WorkManager
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.FragmentManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageLoaderActivity: AppCompatActivity() {
    private val toastUtil = ShowCustomToastImpl()
    private val networkUtil = InternetAvailableImpl()
    private val mapperDrawer = mutableMapOf(R.id.drawer_item_your_account to AccountActivity::class.java)

    private var youFragment = YouFragment()
    private var mapFragment = MapFragment()
    private var groupFragment = GroupFragment()
    private var calendarFragment = CalendarFragment()
    private var mapperFragment = mutableMapOf<Int, Fragment>()

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout
    private lateinit var viewModel: MemoViewModel
    private lateinit var receiver: ConnectivityReceiver
    private lateinit var viewModelGeofence: GeofenceViewModel
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var newActivityButton: FloatingActionButton
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_loader)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.page_loader_fragment, youFragment)
            add(R.id.page_loader_fragment, calendarFragment)
            add(R.id.page_loader_fragment, mapFragment)
            add(R.id.page_loader_fragment, groupFragment)
            commit()
        }

        mapperFragment = mutableMapOf(
            R.id.navbar_item_you to youFragment,
            R.id.navbar_item_calendar to calendarFragment,
            R.id.navbar_item_map to mapFragment,
            R.id.navbar_item_group to groupFragment
        )

        checkConnectivity()
        checkServicesActive()

        registerReceiver()

        setUpActionBar()
        setUpFloatingButton()
        setUpBottomNavigationView()

        if (intent.hasExtra("FRAGMENT_TO_SHOW")) {
            val key = intent.getIntExtra("FRAGMENT_TO_SHOW", 0)
            switchFragment(key, mapperFragment.getValue(key))
        }

        val db = BrockDB.getInstance(this)
        val viewModelFactory = MemoViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory)[MemoViewModel::class.java]

        checkCurrentMemos()

        viewModel.getMemos(LocalDate.now().toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun registerReceiver() {
        receiver = ConnectivityReceiver(this)

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun checkConnectivity() {
        if (networkUtil.isInternetActive(this)) {
            MyNetwork.isConnected = true
        } else {
            MyNetwork.isConnected = false
            toastUtil.showWarningToast(
                "You are offline, check the settings",
                this
            )
        }
    }

    private fun checkServicesActive() {
        if (checkGeofenceService()) {
            if (!MyGeofence.getStatus()) {
                startGeofenceTransition()
            } else {
                toastUtil.showBasicToast(
                    "Geofence service is already active",
                    this
                )
            }
        } else {
            toastUtil.showWarningToast(
                "Geofence service is not active",
                this
            )
        }

        if (checkActivityRecognitionService()) {
            if (!MyActivityRecognition.getStatus()) {
                startActivityRecognition()
            } else {
                toastUtil.showBasicToast(
                    "Recognition service is already active",
                    this
                )
            }
        } else {
            toastUtil.showWarningToast(
                "Recognition service is not active",
                this
            )
        }
    }

    private fun checkGeofenceService(): Boolean {
        return MySharedPreferences.checkService("GEOFENCE_TRANSITION", this) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkActivityRecognitionService(): Boolean {
        return MySharedPreferences.checkService("ACTIVITY_RECOGNITION", this) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }

    private fun startGeofenceTransition() {
        val db = BrockDB.getInstance(this)
        val factoryViewModelGeofence = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryViewModelGeofence)[GeofenceViewModel::class.java]

        // Observer used to work around a fatal error caused by the absence of geofence areas
        observeGeofenceAreas()

        viewModelGeofence.fetchStaticGeofenceAreas()
    }

    private fun observeGeofenceAreas() {
        viewModelGeofence.staticAreas.observe(this) { items ->
            if (!items.isNullOrEmpty()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    MyGeofence.defineRadius(this)
                    MyGeofence.defineAreas(items)

                    val request = MyGeofence.getRequest()
                    val pendingIntent = MyGeofence.getPendingIntent(this)
                    val geofencingClient = LocationServices.getGeofencingClient(this)

                    geofencingClient.addGeofences(request, pendingIntent).run {
                        addOnSuccessListener {
                            MyGeofence.setStatus(true)
                        }
                        addOnFailureListener {
                            Log.e("PAGE_LOADER_ACTIVITY", "Unsuccessful connection")
                        }
                    }
                }
            } else {
                Log.wtf("PAGE_LOADER_ACTIVITY", "Permission geofence transition has denied")
            }
        }
    }

    private fun startActivityRecognition() {
        MyActivityRecognition.getTask(this)?.run {
            addOnSuccessListener {
                MyActivityRecognition.setStatus(true)
            }
            addOnFailureListener {
                Log.d("PAGE_LOADER_ACTIVITY", "Unsuccessful connection")
            }
        }
    }

    private fun setUpActionBar() {
        toolbar = findViewById(R.id.toolbar_page_loader)
        drawer = findViewById(R.id.drawer_page_loader)

        toolbar.run {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )

        actionBarDrawerToggle.run {
            drawerArrowDrawable.color = ContextCompat.getColor(applicationContext, R.color.white)
        }

        drawer.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        findViewById<NavigationView>(R.id.navigation_view_page_loader).setNavigationItemSelectedListener { item ->
            startActivity(Intent(this, mapperDrawer[item.itemId]))
            finish()
            true
        }
    }

    private fun setUpFloatingButton() {
        settingsButton = findViewById(R.id.button_settings)
        newActivityButton = findViewById(R.id.button_new_activity)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        newActivityButton.setOnClickListener {
            Intent(this, ActivityRecognitionService::class.java).also {
                it.action = ActivityRecognitionService.Actions.TERMINATE.toString()
                startService(it)
            }

            Intent(this, NewUserActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    private fun setUpBottomNavigationView() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation_view).setOnItemSelectedListener { item ->
            switchFragment(item.itemId, mapperFragment.getValue(item.itemId))
            true
        }
    }

    private fun switchFragment(key: Int, fragment: Fragment) {
        hideButton(key)
        hideAllFragment(supportFragmentManager)

        supportFragmentManager.beginTransaction().apply {
            show(fragment)
            commit()
        }
    }

    private fun hideButton(item: Int) {
        if (item == R.id.navbar_item_you) {
            newActivityButton.show()
        } else {
            newActivityButton.hide()
        }
    }

    private fun hideAllFragment(manager: FragmentManager) {
        manager.beginTransaction().apply {
            mapperFragment.forEach { (_, value) ->
                hide(value)
            }

            commit()
        }
    }

    private fun checkCurrentMemos() {
        viewModel.memos.observe(this) { items ->
            if (!items.isNullOrEmpty()) {
                OneTimeWorkRequestBuilder<DailyMemoWorker>().build().also {
                    WorkManager.getInstance(this).enqueue(it)
                }
            }
        }
    }
}