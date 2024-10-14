package com.example.brockapp.page.you

import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import com.example.brockapp.page.GeofencePage

class YouGeofencePage: GeofencePage() {
    override fun showCardView(cardView: CardView) {
        cardView.visibility = View.VISIBLE
    }

    override fun observeGeofenceTransitions() {
        geofenceViewModel.geofenceTransitions.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                val transitions = getGroupedTransitions(items)
                populateRecyclerView(transitions)
            } else {
                Log.d("GEOFENCE_PAGE", "No one user's transitions retrieved")
            }
        }
    }

    override fun loadGeofenceTransitions() {
        geofenceViewModel.getGeofenceTransitions()
    }
}