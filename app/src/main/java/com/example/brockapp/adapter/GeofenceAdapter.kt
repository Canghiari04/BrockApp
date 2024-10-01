package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.TransitionAverage

import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GeofenceAdapter(private val areas: List<TransitionAverage>): RecyclerView.Adapter<GeofenceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeofenceViewHolder {
        val geofenceItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_geofence, parent, false)
        return GeofenceViewHolder(geofenceItem)
    }

    override fun getItemCount(): Int {
        return areas.size
    }

    override fun onBindViewHolder(holder: GeofenceViewHolder, position: Int) {
        holder.progressBar.visibility = View.VISIBLE

        holder.initMapView()
        holder.bindGeofence(areas[position])
    }
}