package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.TransitionAverage

import android.view.View
import android.widget.TextView
import android.widget.ProgressBar
import androidx.core.text.HtmlCompat
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions

class GeofenceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), OnMapReadyCallback {
    var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar_cell_geofence)

    private var latitude = 0.0
    private var longitude = 0.0
    private var geofenceMap = itemView.findViewById<MapView>(R.id.map_view_geofence)
    private var geofenceHours = itemView.findViewById<TextView>(R.id.text_view_spent_time)
    private var geofenceCount = itemView.findViewById<TextView>(R.id.text_view_access_count)
    private var geofenceTitle = itemView.findViewById<TextView>(R.id.text_view_title_geofence)

    private lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Iif the map is ready I put the coordinates for the geofence area
        setMapLocation(LatLng(latitude, longitude))

        map.setOnMapLoadedCallback {
            progressBar.visibility = View.GONE
        }

        map.uiSettings.run {
            isZoomGesturesEnabled = false
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isMapToolbarEnabled = false
        }
    }

    fun initMapView() {
        geofenceMap.onCreate(null)
        geofenceMap.getMapAsync(this)
    }

    fun bindGeofence(transition: TransitionAverage) {
        geofenceTitle.setText(transition.nameLocation)
        geofenceHours.setText(
            HtmlCompat
                .fromHtml(
                    "Average time spent: <b>${transition.averageTime}</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        )
        geofenceCount.setText(
            HtmlCompat
                .fromHtml(
                    "Number of access: <b>${transition.count}</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        )

        latitude = transition.latitude
        longitude = transition.longitude
    }

    private fun setMapLocation(location: LatLng) {
        if (::map.isInitialized) {
            map.clear()

            map.addMarker(
                MarkerOptions()
                    .position(location)
            )

            map.moveCamera(
                CameraUpdateFactory.
                newLatLngZoom(
                    location,
                    14f)
            )
        }
    }
}