package com.example.brockapp.fragment

import android.graphics.Camera
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.GeofenceAreaEntry
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.GeofenceViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MapFragment: Fragment(R.layout.map_fragment), OnMapReadyCallback {
    private lateinit var db: BrockDB
    private lateinit var map: GoogleMap
    private lateinit var geofence: MyGeofence
    private lateinit var viewModel: GeofenceViewModel
    private lateinit var mapFragment: SupportMapFragment

    private var mapMarker = mutableMapOf<String, LatLng>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofence = MyGeofence.getInstance()

        mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db = BrockDB.getInstance(requireContext())
        val factoryViewModel = GeofenceViewModelFactory(db)

        viewModel = ViewModelProvider(this, factoryViewModel)[GeofenceViewModel::class.java]

        observeGeofenceAreas()

        val input = view.findViewById<EditText>(R.id.text_new_area)

        view.findViewById<FloatingActionButton>(R.id.button_plus_geofence).setOnClickListener {
            val newArea = input.text.toString()

            if (newArea.isNotEmpty()) {
                val (address, location) = getAddressAndLocation(newArea)

                if (address != null && location != null) {
                    val geofenceArea = GeofenceAreaEntry(
                        longitude = location.longitude,
                        latitude = location.latitude,
                        name = address.featureName
                    )

                    viewModel.insertGeofenceArea(geofenceArea)
                } else {
                    Toast.makeText(requireContext(), R.string.toast_error_map, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val startingPoint = LatLng(41.8719, 12.5674)
        val position = CameraPosition.Builder()
            .target(startingPoint)
            .zoom(5.5f)
            .build()

        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(position)
        )

        populateMapOfMarker()
    }

    private fun observeGeofenceAreas() {
        viewModel.areas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                for (area in areas) {
                    val marker = LatLng(area.latitude, area.longitude)
                    mapMarker.put(area.name, marker)
                }
            } else {
                Log.d("MAP_FRAGMENT", "No geofence areas")
            }
        }
    }

    private fun populateMapOfMarker() {
        mapMarker.forEach { (key, value) ->
            map.addMarker(
                MarkerOptions()
                    .position(value)
                    .title(key)
            )

            map.addCircle(
                CircleOptions()
                    .center(value)
                    .radius(geofence.radius.toDouble())
                    .strokeColor(Color.RED)
                    .strokeWidth(2.5f)
            )
        }
    }

    private fun getAddressAndLocation(item: String): Pair<Address?, LatLng?> {
        var address = Address(Locale.getDefault())
        var location = LatLng(0.0, 0.0)

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocationName(item, 1)

            if (addresses != null) {
                address = addresses[0]
                location = LatLng(address.latitude, address.longitude)
            } else {
                return Pair(null, null)
            }
        } catch (e: Exception)  {
            Log.e("MAP_FRAGMENT", e.toString())
        }

        return Pair(address, location)
    }
}