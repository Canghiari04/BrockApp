package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.viewModel.MemoViewModel
import com.example.brockapp.worker.MemoNotifierWorker
import com.example.brockapp.viewModel.GeofenceViewModel
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.viewModel.MemoViewModelFactory
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.singleton.MyActivityRecognition
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.service.ActivityRecognitionService
import com.example.brockapp.viewModel.GeofenceViewModelFactory

import android.Manifest
import android.util.Log
import android.os.Bundle
import java.time.LocalDate
import android.view.MenuItem
import android.content.Intent
import androidx.work.WorkManager
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.ui.navigateUp
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity: AppCompatActivity() {

    private val toastUtil = ShowCustomToastImpl()
    private val networkUtil = InternetAvailableImpl()

    private lateinit var navController: NavController
    private lateinit var viewModelMemo: MemoViewModel
    private lateinit var receiver: ConnectivityReceiver
    private lateinit var viewModelGeofence: GeofenceViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        val db = BrockDB.getInstance(this)
        val viewModelFactory = MemoViewModelFactory(db)
        viewModelMemo = ViewModelProvider(this, viewModelFactory)[MemoViewModel::class.java]

        setupNavigation()

        checkConnectivity()
        checkCurrentMemos()

        registerReceiver()

        viewModelMemo.getMemos(LocalDate.now().toString())
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        checkServices()
    }

    override fun onDestroy() {
        super.onDestroy()

        val intent = Intent(this, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.Actions.TERMINATE.toString()
        }

        if (MyActivityRecognition.getStatus()) {
            startService(intent)
        }

        unregisterReceiver(receiver)
    }

    private fun setupNavigation() {
        val navDrawer = findViewById<NavigationView>(R.id.drawer_navigation_view)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_main)
        val bottomBar = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navbar_item_you, R.id.navbar_item_map, R.id.navbar_item_calendar, R.id.navbar_item_group),
            drawer
        )

        setupActionBarWithNavController(
            navController,
            appBarConfiguration
        )

        navDrawer.setupWithNavController(navController)

        navDrawer.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            drawer.closeDrawers()

            when (item.itemId) {
                R.id.drawer_item_account -> {
                    navController.navigate(R.id.drawer_item_account)
                    true
                }

                else -> {
                    false
                }
            }
        }

        bottomBar.setupWithNavController(navController)
    }

    private fun checkServices() {
        if (checkGeofenceService()) {
            if (!MyGeofence.getStatus()) {
                startGeofenceTransition()
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

    private fun startGeofenceTransition() {
        val db = BrockDB.getInstance(this)
        val factoryViewModelGeofence = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryViewModelGeofence)[GeofenceViewModel::class.java]

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

    private fun checkActivityRecognitionService(): Boolean {
        return MySharedPreferences.checkService("ACTIVITY_RECOGNITION", this) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }

    private fun startActivityRecognition() {
        MyActivityRecognition.getTask(this)?.run {
            addOnSuccessListener {
                MyActivityRecognition.setStatus(true)
                startActivityRecognitionService()
            }
            addOnFailureListener {
                Log.d("PAGE_LOADER_ACTIVITY", "Unsuccessful connection")
            }
        }
    }

    private fun startActivityRecognitionService() {
        val intent = Intent(this, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.Actions.START.toString()
        }

        startService(intent)
    }

    private fun checkConnectivity() {
        MyNetwork.isConnected = networkUtil.isInternetActive(this)
    }

    private fun checkCurrentMemos() {
        viewModelMemo.memos.observe(this) { items ->
            if (!items.isNullOrEmpty()) {
                OneTimeWorkRequestBuilder<MemoNotifierWorker>().build().also {
                    WorkManager.getInstance(this).enqueue(it)
                }
            }
        }
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
}