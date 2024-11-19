package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.data.Area
import com.example.brockapp.BuildConfig
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.dialog.MarkerDialog
import com.example.brockapp.viewModel.MapViewModel
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.room.GeofenceAreasEntity
import com.example.brockapp.viewModel.NetworkViewModel
import com.example.brockapp.viewModel.GeofenceViewModel
import com.example.brockapp.viewModel.MapViewModelFactory
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.viewModel.GeofenceViewModelFactory

import android.net.Uri
import android.Manifest
import android.util.Log
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.app.AlertDialog
import android.location.Location
import android.location.Geocoder
import android.provider.Settings
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.widget.ProgressBar
import androidx.compose.ui.unit.dp
import android.view.LayoutInflater
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.fragment.app.Fragment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.TextField
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.res.colorResource
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.LocalTextStyle
import androidx.core.content.res.ResourcesCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.shape.CircleShape
import com.google.android.gms.tasks.CancellationToken
import androidx.compose.foundation.layout.fillMaxSize
import com.google.android.gms.location.LocationResult
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.layout.fillMaxWidth
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.android.gms.tasks.OnTokenCanceledListener
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient

class MapFragment: Fragment() {
    
    private var lastMarker: Marker? = null
    private var lastKnownLocation: Location? = null

    private val toastUtil = ShowCustomToastImpl()
    private val requestLocationPermissionLauncher = 
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val areGranted = run {
                permissions.filter { !it.value }.keys.isEmpty()
            }

            when {
                areGranted -> {
                    getLastKnownLocation()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    showRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            } 
        }

    private lateinit var db: BrockDB
    private lateinit var map: MapView
    private lateinit var button: ComposeView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchLocation: ComposeView
    private lateinit var locationRequest: LocationRequest
    private lateinit var geofenceArea: GeofenceAreasEntity
    private lateinit var locationCallback: LocationCallback
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        db = BrockDB.getInstance(requireContext())
        val factoryViewModel = GeofenceViewModelFactory(db)

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]
        viewModelGeofence = ViewModelProvider(this, factoryViewModel)[GeofenceViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        progressBar = rootView.findViewById(R.id.progress_bar_map_fragment)
        searchLocation = rootView.findViewById(R.id.text_view_search)
        button = rootView.findViewById(R.id.button_geo_localization)

        progressBar.visibility = View.VISIBLE

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.container_view_map)

        map.apply {
            setMultiTouchControls(true)
            controller.setZoom(7.0)
            controller.setCenter(
                GeoPoint(
                    41.8719,
                    12.5674
                )
            )
        }

        searchLocation.setContent {
            DefineSearchTextView()
        }

        button.setContent {
            FloatingActionButton(
                modifier = Modifier.padding(0.dp, 0.dp, 24.dp, 16.dp),
                onClick = {
                    requestLocationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                containerColor = colorResource(id = R.color.uni_red),
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Marker",
                    tint = Color.White
                )
            }
        }

        observeNetwork()
        observeStaticGeofenceAreas()
        observeUpdatesGeofenceAreas()
        observeCheckGeofenceAreaAlreadyIn()

        viewModelGeofence.fetchStaticGeofenceAreas()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @Composable
    private fun DefineSearchTextView() {
        val geocoder = Geocoder(requireContext())

        val viewModelFactory = MapViewModelFactory(geocoder)
        val viewModelMap = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]

        val suggestions by viewModelMap.suggestions.collectAsState()
        val searchText by viewModelMap.searchText.collectAsState()
        val isSearching by viewModelMap.isSearching.collectAsState()

        Column (
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = searchText,
                onValueChange = { viewModelMap.onSearchTextChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "Search a new location",
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = if (searchText.isNotBlank()) {
                    RoundedCornerShape(6.dp, 6.dp, 0.dp, 0.dp)
                } else {
                    RoundedCornerShape(6.dp)
                },
                colors = TextFieldDefaults. colors(
                    focusedContainerColor = Color.White,
                    errorIndicatorColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = colorResource(id = R.color.grey)
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = colorResource(id = R.color.uni_red)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .background(Color.White)
                        .clip(RoundedCornerShape( 0.dp, 0.dp, 6.dp, 6.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    suggestions?.let {
                        val keys = it.keys.toList()

                        items(keys) { item ->
                            Text(
                                text = item,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .clickable { addNewArea(it[item]!!) },
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun addNewArea(item: Area) {
        geofenceArea = GeofenceAreasEntity(
            username = MyUser.username,
            longitude = item.address.longitude,
            latitude = item.address.latitude,
            name = item.address.featureName
        )

        viewModelGeofence.checkGeofenceAreaAlreadyIn(geofenceArea)
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && isLocationFresh(location)) {
                addCurrentMarker(location)
            } else {
                getCurrentLocation()
            }
        }

        fusedLocationClient.lastLocation.addOnFailureListener {
            getCurrentLocation()
        }
    }
    
    private fun isLocationFresh(location: Location): Boolean {
        return (System.currentTimeMillis() - location.time) < 15000
    }

    private fun addCurrentMarker(location: Location) {
        map.apply {
            val geoPoint = GeoPoint(
                location.latitude,
                location.longitude
            )

            controller.setZoom(16.0)
            controller.setCenter(geoPoint)

            val marker = Marker(map).apply {
                position = geoPoint
                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )
                icon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.geo_localization_marker,
                    null
                )
            }

            lastMarker?.let {
                overlays.remove(it)
            }

            lastMarker = marker
            lastKnownLocation = location

            setupCurrentMarker(marker)
            overlays.add(marker)
        }
    }

    private fun setupCurrentMarker(item: Marker) {
        item.setOnMarkerClickListener { marker, _ ->
            activity?.let {
                MarkerDialog(
                    marker,
                    map,
                    viewModelGeofence
                ).show(it.supportFragmentManager, "CUSTOM_MARKER_DIALOG")
            }

            true
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                    return this
                }

                override fun isCancellationRequested(): Boolean {
                    return false
                }
            }
        ).addOnSuccessListener { currentLocation ->
            if (currentLocation != null) {
                addCurrentMarker(currentLocation)
            } else {
                startLocationUpdates()
            }
        }.addOnFailureListener {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                locationResult.lastLocation?.let {
                    addCurrentMarker(it)
                } ?: {
                    toastUtil.showWarningToast(
                        "Not possible to locate your position",
                        requireContext()
                    )
                }

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.rationale_permissions_title)
            .setMessage(R.string.rationale_location_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.setting_permission_title)
            .setMessage(R.string.settings_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requireContext().startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireActivity().packageName, null)
                    )
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(viewLifecycleOwner) { item ->
            map.visibility = if (item) View.VISIBLE else View.GONE
            button.visibility = if (item) View.VISIBLE else View.GONE
            progressBar.visibility = if (!item) View.VISIBLE else View.GONE
            searchLocation.visibility = if (item) View.VISIBLE else View.GONE
        }
    }

    private fun observeStaticGeofenceAreas() {
        viewModelGeofence.staticAreas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                val mapMarker = mutableMapOf<String, GeoPoint>()

                for (area in areas) {
                    mapMarker[area.name] = GeoPoint(
                        area.latitude,
                        area.longitude
                    )
                }

                populateMapOfMarker(mapMarker)
            } else {
                Log.d("MAP_FRAGMENT", "No areas found")
            }
        }
    }

    private fun populateMapOfMarker(mapMarker: Map<String, GeoPoint>) {
        mapMarker.forEach { (key, value) ->
            val marker = Marker(map).apply {
                title = key
                position = value
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = resources.getDrawable(R.drawable.map_marker_icon)
            }

            setupMarker(marker)

            map.overlays.add(marker)
        }
    }

    private fun setupMarker(item: Marker) {
        item.setOnMarkerClickListener { marker, _ ->
            activity?.let {
                MarkerDialog(
                    marker,
                    map,
                    viewModelGeofence
                ).show(it.supportFragmentManager, "CUSTOM_MARKER_DIALOG")
            }

            true
        }
    }

    private fun observeUpdatesGeofenceAreas() {
        viewModelGeofence.updateAreas.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty() && MySharedPreferences.checkService("GEOFENCE_TRANSITION", requireContext())) {
                val intent = Intent(requireContext(), GeofenceService::class.java).apply {
                    action = GeofenceService.Actions.RESTART.toString()
                }

                requireActivity().startService(intent)
            }
        }
    }

    private fun observeCheckGeofenceAreaAlreadyIn() {
        viewModelGeofence.isAlreadyIn.observe(viewLifecycleOwner) {
            if (!it) {
                addNewMarker(geofenceArea)
                viewModelGeofence.insertGeofenceArea(geofenceArea)
            } else {
                toastUtil.showWarningToast(
                    "Geofence area is already present",
                    requireContext()
                )

                map.apply {
                    controller.setZoom(16.0)
                    controller.setCenter(
                        GeoPoint(
                            geofenceArea.latitude,
                            geofenceArea.longitude
                        )
                    )
                }
            }
        }
    }

    private fun addNewMarker(geofenceArea: GeofenceAreasEntity) {
        map.apply {
            val geoPoint = GeoPoint(
                geofenceArea.latitude,
                geofenceArea.longitude
            )

            controller.setZoom(16.0)
            controller.setCenter(geoPoint)

            val marker = Marker(map).apply {
                position = geoPoint
                title = geofenceArea.name
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = resources.getDrawable(R.drawable.map_marker_icon)
            }

            setupMarker(marker)
            overlays.add(marker)
        }
    }
}