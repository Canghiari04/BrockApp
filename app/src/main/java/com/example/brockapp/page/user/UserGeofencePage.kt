package com.example.brockapp.page.user

import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.page.GeofencePage
import com.example.brockapp.extraObject.MyUser

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

        groupViewModel.getCurrentFriends(MyUser.id)
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
        groupViewModel.errorAddFriend.observe(this) {
            if (it) {
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
        groupViewModel.errorDeleteFriend.observe(this) {
            if (it) {
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
        groupViewModel.currentFriends.observe(viewLifecycleOwner) { items ->
            if (items.contains(friend.username)) {
                buttonUser.setText("REMOVE")
                buttonUser.setOnClickListener { groupViewModel.deleteFriend(friend.username) }
            } else {
                buttonUser.setOnClickListener { groupViewModel.addFriend(friend.username) }
            }
        }
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