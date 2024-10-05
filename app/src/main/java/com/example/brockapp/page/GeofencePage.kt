package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.TransitionAverage
import com.example.brockapp.adapter.GeofenceAdapter
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.database.GeofenceTransitionEntity
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlin.time.Duration.Companion.milliseconds
import androidx.recyclerview.widget.LinearLayoutManager

abstract class GeofencePage: Fragment(R.layout.page_geofence) {
    private lateinit var recyclerView: RecyclerView

    protected lateinit var viewModel: GeofenceViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_welcome_geofence).text =
            ("Geofence Area Overview: Here you'll find all the geofenced areas you've accessed. If your list is currently empty, it's time to dive in and create your first geofence!")

        recyclerView = view.findViewById(R.id.recycler_view_page_geofence)

        val db = BrockDB.getInstance(requireContext())
        val factoryViewModel = GeofenceViewModelFactory(db)
        viewModel = ViewModelProvider(this, factoryViewModel)[GeofenceViewModel::class.java]

        observeGeofenceTransitions()

        loadGeofenceTransitions()
    }

    private fun observeGeofenceTransitions() {
        viewModel.geofenceTransitions.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                val transitions = getGroupedTransitions(items)
                populateRecyclerView(transitions)
            } else {
                Log.d("GEOFENCE_PAGE", "No one transitions retrieved")
            }
        }
    }

    private fun getGroupedTransitions(items: List<GeofenceTransitionEntity>): List<TransitionAverage> {
        val groupedByLocation = items.groupBy { it.nameLocation }

        return groupedByLocation.map { (locationName, locationList) ->
            val firstLocation = locationList.first()

            val totalDurationMillis = locationList.fold(0L) { acc, location ->
                val timeSpent = location.exitTime - location.arrivalTime
                acc + timeSpent
            }

            val averageDurationMillis = totalDurationMillis / locationList.size
            val averageDuration = averageDurationMillis.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            TransitionAverage(
                nameLocation = locationName,
                latitude = firstLocation.latitude,
                longitude = firstLocation.longitude,
                averageTime = averageDuration,
                count = locationList.size.toLong()
            )
        }
    }

    private fun populateRecyclerView(transitions: List<TransitionAverage>) {
        val adapter = GeofenceAdapter(transitions)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    protected abstract fun loadGeofenceTransitions()
}