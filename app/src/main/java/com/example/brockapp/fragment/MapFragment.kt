package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.service.MapService
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.database.GeofenceAreaEntry
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import android.util.Log
import java.util.Locale
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.launch
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.example.brockapp.dialog.MarkerDialog
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition

class MapFragment: Fragment(R.layout.fragment_map), OnMapReadyCallback {
    private lateinit var db: BrockDB
    private lateinit var map: GoogleMap
    private lateinit var geofence: MyGeofence
    private lateinit var viewModel: GeofenceViewModel
    private lateinit var mapFragment: SupportMapFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofence = MyGeofence.getInstance()

        mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db = BrockDB.getInstance(requireContext())
        val factoryViewModel = GeofenceViewModelFactory(db)

        viewModel = ViewModelProvider(this, factoryViewModel)[GeofenceViewModel::class.java]

        observeUpdatesGeofenceAreas()

        val input = view.findViewById<AutoCompleteTextView>(R.id.text_new_area)

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
                val geofenceArea = GeofenceAreaEntry(
                    longitude = location.longitude,
                    latitude = location.latitude,
                    name = address.featureName
                )

                addNewMarker(geofenceArea)
                viewModel.insertGeofenceArea(geofenceArea)
            } else {
                Toast.makeText(requireContext(), R.string.toast_error_map, Toast.LENGTH_LONG).show()
            }

            input.setText(R.string.text_blank)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        observeInitialGeofenceAreas()

        val startingPoint = LatLng(41.8719, 12.5674)
        val position = CameraPosition.Builder()
            .target(startingPoint)
            .zoom(5.5f)
            .build()

        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(position)
        )

        map.setOnMarkerClickListener { marker ->
            activity?.let { MarkerDialog(marker, viewModel).show(it.supportFragmentManager, "CUSTOM_MARKER_DIALOG") }
            true
        }
    }

    private fun observeInitialGeofenceAreas() {
        val mapMarker = mutableMapOf<String, LatLng>()

        viewModel.staticAreas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                for (area in areas) {
                    val coordinates = LatLng(area.latitude, area.longitude)
                    mapMarker[area.name] = coordinates
                }

                populateMapOfMarker(mapMarker)
            } else {
                Log.d("MAP_FRAGMENT", "No geofence areas")
            }
        }
    }

    private fun observeUpdatesGeofenceAreas() {
        viewModel.dynamicAreas.observe(viewLifecycleOwner) { areas ->
            geofence.geofences = areas

            val serviceIntent = Intent(requireContext(), MapService::class.java)
            activity?.startService(serviceIntent)
        }
    }

    private fun showSuggestions(query: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocationName(query, 3)
                val suggestions: MutableList<String> = mutableListOf()

                addresses?.let {
                    for (address in it) {
                        suggestions.add(address.getAddressLine(0))
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        suggestions
                    )

                    val input = view?.findViewById<AutoCompleteTextView>(R.id.text_new_area)
                    input?.setAdapter(adapter)
                    input?.showDropDown()
                }
            } catch (e: Exception) {
                Log.e("MAP_FRAGMENT", e.toString())
            }
        }
    }

    private fun addNewMarker(geofenceArea: GeofenceAreaEntry) {
        val coordinates = LatLng(geofenceArea.latitude, geofenceArea.longitude)

        map.addMarker(
            MarkerOptions()
                .position(coordinates)
                .title(geofenceArea.name)
        )

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f))
    }

    private fun populateMapOfMarker(mapMarker: Map<String, LatLng>) {
        mapMarker.forEach { (key, value) ->
            map.addMarker(
                MarkerOptions()
                    .position(value)
                    .title(key)
            )
        }
    }

    private fun getAddressAndLocation(item: String): Pair<Address?, LatLng?> {
        var address: Address? = null
        var location: LatLng? = null

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocationName(item, 1)

            if (!addresses.isNullOrEmpty()) {
                address = addresses[0]
                location = LatLng(address.latitude, address.longitude)
            } else {
                return Pair(null, null)
            }
        } catch (e: Exception) {
            Log.e("MAP_FRAGMENT", e.toString())
        }

        return Pair(address, location)
    }
}