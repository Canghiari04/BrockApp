package com.example.brockapp.fragment

import com.example.brockapp.R

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.dialog.MarkerDialog
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File

class FriendMapFragment: Fragment(R.layout.fragment_friend_map), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var viewModel: FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val s3Client = S3ClientProvider.getInstance(requireContext())

        val file = File(requireContext().filesDir, "user_data.json")

        val db = BrockDB.getInstance(requireContext())
        val viewModelFactory = FriendsViewModelFactory(s3Client, db, file)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FriendsViewModel::class.java]

        val mapFragment = childFragmentManager.findFragmentById(R.id.friend_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        observeFriendGeofenceAreas()

        val startingPoint = LatLng(41.8719, 12.5674)
        val position = CameraPosition.Builder()
            .target(startingPoint)
            .zoom(5.5f)
            .build()

        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(position)
        )
    }

    private fun observeFriendGeofenceAreas() {
        val mapMarker = mutableMapOf<String, LatLng>()

        viewModel.friendGeofenceLocalities.observe(viewLifecycleOwner) { areas ->
            if (!areas.isNullOrEmpty()) {
                for (area in areas) {
                    val coordinates = LatLng(area.latitude, area.longitude)
                    mapMarker[area.id] = coordinates
                }

                populateMapOfMarker(mapMarker)
            } else {
                Log.d("FRIEND_MAP_FRAGMENT", "No geofence areas")
            }
        }
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
}