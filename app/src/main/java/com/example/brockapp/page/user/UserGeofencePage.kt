package com.example.brockapp.page.user

import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.page.GeofencePage

import android.util.Log
import android.view.View
import android.widget.TextView

class UserGeofencePage(private val friend: Friend): GeofencePage() {
    override fun setUpCardView() {
        cardViewYouGeofencePage.visibility = View.GONE
        cardViewUserGeofencePage.visibility = View.VISIBLE

        setUpTextView()

        observeAddedFriend()
        observeRemovedFriend()
        observeCurrentFriends()

        viewModelGroup.getCurrentFriends()
    }

    private fun setUpTextView() {
        requireView().findViewById<TextView>(R.id.text_view_user_user_name).also {
            it.text = friend.username
        }

        requireView().findViewById<TextView>(R.id.text_view_user_address).also {
            it.text = "${friend.country}, ${friend.city}"
        }
    }

    private fun observeAddedFriend() {
        viewModelGroup.errorAddFriend.observe(this) {
            if (it) {
                buttonUser.setText("REMOVE")
                toastUtil.showBasicToast(
                    "${friend.username} is your new friend",
                    requireContext()
                )
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while adding",
                    requireContext()
                )
            }
        }
    }

    private fun observeRemovedFriend() {
        viewModelGroup.errorDeleteFriend.observe(this) {
            if (it) {
                buttonUser.setText("FOLLOW")
                toastUtil.showBasicToast(
                    "${friend.username} has been removed",
                    requireContext()
                )
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while removing",
                    requireContext()
                )
            }
        }
    }

    private fun observeCurrentFriends() {
        viewModelGroup.currentFriends.observe(viewLifecycleOwner) { items ->
            if (items.contains(friend.username)) {
                buttonUser.setText("REMOVE")
                buttonUser.setOnClickListener { viewModelGroup.deleteFriend(friend.username) }
            } else {
                buttonUser.setText("FOLLOW")
                buttonUser.setOnClickListener { viewModelGroup.addFriend(friend.username) }
            }
        }
    }

    override fun observeGeofenceTransitions() {
        viewModelGroup.userGeofenceTransitions.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                val transitions = getGroupedTransitions(items)
                populateSpinnerNames(transitions)
            } else {
                Log.d("GEOFENCE_PAGE", "No one friend's transitions retrieved")
            }
        }
    }

    override fun loadGeofenceTransitions(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserGeofenceTransitions(startOfPeriod, endOfPeriod, friend)
    }
}