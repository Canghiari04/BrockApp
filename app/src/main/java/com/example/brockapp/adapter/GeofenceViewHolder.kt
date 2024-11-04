package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.TransitionAverage

import android.view.View
import android.widget.TextView
import android.widget.ProgressBar
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import androidx.core.text.HtmlCompat
import org.osmdroid.views.overlay.Marker
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class GeofenceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar_cell_geofence)

    private var map = itemView.findViewById<MapView>(R.id.map_view_geofence)
    private var hours = itemView.findViewById<TextView>(R.id.text_view_spent_time)
    private var count = itemView.findViewById<TextView>(R.id.text_view_access_count)
    private var title = itemView.findViewById<TextView>(R.id.text_view_title_geofence)

    fun bindGeofence(transition: TransitionAverage) {
        title.setText(transition.nameLocation)

        hours.setText(
            HtmlCompat
                .fromHtml(
                    "Average time spent: <b>${transition.averageTime}</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        )

        count.setText(
            HtmlCompat
                .fromHtml(
                    "Number of access: <b>${transition.count}</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        )

        map.also {
            val geoPoint = GeoPoint(
                transition.latitude,
                transition.longitude
            )

            it.setMultiTouchControls(false)
            it.controller.setZoom(16.0)
            it.controller.setCenter(geoPoint)

            val marker = Marker(map).also {
                it.position = geoPoint
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                it.icon = ContextCompat.getDrawable(itemView.context, R.drawable.map_marker_icon)
            }

            marker.setOnMarkerClickListener { _, _ -> true }

            it.overlays.add(marker)
        }
    }
}