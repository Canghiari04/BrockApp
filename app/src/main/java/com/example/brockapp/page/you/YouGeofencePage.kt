package com.example.brockapp.page.you

import com.example.brockapp.page.GeofencePage

import android.util.Log
import android.view.View

class YouGeofencePage: GeofencePage() {
    override fun setUpCardView() {
        cardViewUserGeofencePage.visibility = View.GONE
        cardViewYouGeofencePage.visibility = View.VISIBLE
    }

    override fun observeGeofenceTransitions() {
        viewModelGeofence.geofenceTransitions.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                val transitions = getGroupedTransitions(items)
                populateSpinnerNames(transitions)
            } else {
                Log.d("GEOFENCE_PAGE", "No one user's transitions retrieved")
            }
        }
    }

    override fun loadGeofenceTransitions(startOfPeriod: String, endOfPeriod: String) {
        viewModelGeofence.getGeofenceTransitions(
            startOfPeriod,
            endOfPeriod
        )
    }
}