package com.example.brockapp.page.friend

import com.example.brockapp.data.Friend

import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import com.example.brockapp.page.GeofencePage

class FriendGeofencePage(private val friend: Friend): GeofencePage() {
    override fun showCardView(cardView: CardView) {
        cardView.visibility = View.GONE
    }

    override fun observeGeofenceTransitions() {
        groupViewModel.friendGeofenceTransitions.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                val transitions = getGroupedTransitions(items)
                populateRecyclerView(transitions)
            } else {
                Log.d("GEOFENCE_PAGE", "No one friend's transitions retrieved")
            }
        }
    }

    override fun loadGeofenceTransitions() {
        groupViewModel.getFriendGeofenceTransitions(friend)
    }
}