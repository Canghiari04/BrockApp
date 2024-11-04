package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.BuildConfig
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.dialog.MarkerDialog
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.room.GeofenceAreasEntity
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.viewmodel.GeofenceViewModelFactory
import com.example.brockapp.util.AccessFineLocationPermissionUtil

import android.Manifest
import android.util.Log
import java.util.Locale
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.location.Address
import android.location.Location
import kotlinx.coroutines.launch
import android.location.Geocoder
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.widget.ProgressBar
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import android.content.pm.PackageManager
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Priority
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment: Fragment() {
    private var lastKnownMarker: Marker? = null
    private var lastKnownLocation: Location? = null

    private val toastUtil = ShowCustomToastImpl()
    private val networkUtil = InternetAvailableImpl()

    private lateinit var db: BrockDB
    private lateinit var map: MapView
    private lateinit var progressBar: ProgressBar
    private lateinit var input: AutoCompleteTextView
    private lateinit var button: FloatingActionButton
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        db = BrockDB.getInstance(requireContext())
        val factoryViewModel = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryViewModel)[GeofenceViewModel::class.java]

        // View model is associated to the activity, cause inside the activity has been registered the Connectivity receiver
        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        progressBar = rootView.findViewById(R.id.progress_bar_map_fragment)
        button = rootView.findViewById(R.id.button_geo_localization)
        input = rootView.findViewById(R.id.text_new_area)

        progressBar.visibility = View.VISIBLE

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.container_view_map)
        map.also {
            it.setMultiTouchControls(true)
            it.controller.setZoom(7.0)
            it.controller.setCenter(
                GeoPoint(
                    41.8719,
                    12.5674
                )
            )
        }

        getCurrentLocation()

        observeNetwork()
        observeInitialGeofenceAreas()
        observeUpdatesGeofenceAreas()

        input.addTextChangedListener {
            val userInput = input.text.toString()

            if (userInput.isNotEmpty() && userInput.length >= 3) {
                showSuggestions(userInput)
            }
        }

        input.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocation = parent.getItemAtPosition(position) as String
            val (address, location) = getAddressAndLocation(selectedLocation)

            if (address != null && location != null) {
                val geofenceArea = GeofenceAreasEntity(
                    username = MyUser.username,
                    longitude = location.longitude,
                    latitude = location.latitude,
                    name = address.featureName
                )

                addNewMarker(geofenceArea)
                viewModelGeofence.insertGeofenceArea(geofenceArea)
            } else {
                toastUtil.showBasicToast(
                    "No one location find with this address",
                    requireContext()
                )
            }

            input.setText(R.string.text_blank)
        }

        val utilPermission = AccessFineLocationPermissionUtil(
            requireActivity() as AppCompatActivity
        ) { getCurrentLocation() }

        button.setOnClickListener {
            utilPermission.requestAccessFineLocation()
        }

        viewModelGeofence.fetchStaticGeofenceAreas()
    }

    override fun onResume() {
        super.onResume()
        if (networkUtil.isInternetActive(requireContext())) map.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (networkUtil.isInternetActive(requireContext())) map.onPause()
    }

    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(viewLifecycleOwner) { item ->
            map.visibility = if (item) View.VISIBLE else View.GONE
            input.visibility = if (item) View.VISIBLE else View.GONE
            button.visibility = if (item) View.VISIBLE else View.GONE
            progressBar.visibility = if (!item) View.VISIBLE else View.GONE
        }
    }

    private fun observeInitialGeofenceAreas() {
        viewModelGeofence.staticAreas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                val mapMarker = mutableMapOf<String, GeoPoint>()

                for (area in areas) {
                    val coordinates = GeoPoint(area.latitude, area.longitude)
                    mapMarker[area.name] = coordinates
                }

                populateMapOfMarker(mapMarker)
            } else {
                Log.d("MAP_FRAGMENT", "No one geofence areas retrieved")
            }
        }
    }

    private fun populateMapOfMarker(mapMarker: Map<String, GeoPoint>) {
        mapMarker.forEach { (key, value) ->
            val marker = Marker(map).also {
                it.title = key
                it.position = value
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                it.icon = resources.getDrawable(R.drawable.map_marker_icon)

                setUpMarker(it)
            }

            map.overlays.add(marker)
        }
    }

    private fun setUpMarker(item: Marker) {
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
        viewModelGeofence.dynamicAreas.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty() && MySharedPreferences.checkService("GEOFENCE_TRANSITION", requireContext())) {
                Intent(requireContext(), GeofenceService::class.java).also {
                    it.action = GeofenceService.Actions.RESTART.toString()
                    requireActivity().startService(it)
                }
            }
        }
    }

    private fun showSuggestions(query: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val addresses = geocoder.getFromLocationName(query, 3)
                val suggestions: MutableList<String> = mutableListOf()

                addresses?.let {
                    for (address in it) {
                        suggestions.add(address.getAddressLine(0))
                    }
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    suggestions
                )

                val input = view?.findViewById<AutoCompleteTextView>(R.id.text_new_area)
                input?.setAdapter(adapter)
                input?.showDropDown()
            } catch (e: Exception) {
                Log.e("MAP_FRAGMENT", e.toString())
            }
        }
    }

    private fun getAddressAndLocation(item: String): Pair<Address?, GeoPoint?> {
        var address: Address? = null
        var location: GeoPoint? = null

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocationName(item, 1)

            if (!addresses.isNullOrEmpty()) {
                address = addresses[0]
                location = GeoPoint(address.latitude, address.longitude)
            } else {
                return Pair(null, null)
            }
        } catch (e: Exception) {
            Log.e("MAP_FRAGMENT", e.toString())
        }

        return Pair(address, location)
    }

    private fun addNewMarker(geofenceArea: GeofenceAreasEntity) {
        map.also {
            val geoPoint = GeoPoint(
                geofenceArea.latitude,
                geofenceArea.longitude
            )

            it.controller.setZoom(10.0)
            it.controller.setCenter(geoPoint)

            val marker = Marker(map).also {
                it.position = geoPoint
                it.title = geofenceArea.name
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                it.icon = resources.getDrawable(R.drawable.map_marker_icon)

                setUpMarker(it)
            }

            it.overlays.add(marker)
        }
    }

    private fun setUpCurrentLocationUpdate() {
        locationRequest = LocationRequest
            .Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                15000
            ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                locationResult.locations[0]?.takeIf {
                    lastKnownLocation?.latitude != it.latitude && lastKnownLocation?.longitude != it.longitude
                }?.let { location ->
                    map.also {
                        val geoPoint = GeoPoint(
                            location.latitude,
                            location.longitude
                        )

                        it.controller.setZoom(10.0)
                        it.controller.setCenter(geoPoint)

                        val marker = Marker(map).also {
                            it.position = geoPoint
                            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            it.icon = resources.getDrawable(R.drawable.geo_localization_marker)

                            setUpMarker(it)
                        }

                        lastKnownMarker?.let { item ->
                            map.overlays.remove(item)
                        }

                        lastKnownLocation = location
                        lastKnownMarker = marker

                        map.overlays.add(marker)
                    }
                } ?: {
                    toastUtil.showWarningToast(
                        "Not possible to geocode your position",
                        requireContext()
                    )
                }

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun getCurrentLocation() {
        if (checkPermission()) {
            setUpCurrentLocationUpdate()

            if (::locationRequest.isInitialized && ::locationCallback.isInitialized) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        } else {
            toastUtil.showBasicToast(
                "Geo localization disabled",
                requireContext()
            )
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}