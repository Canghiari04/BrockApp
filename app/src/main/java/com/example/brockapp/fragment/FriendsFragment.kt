package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.adapter.FriendsAdapter
import com.example.brockapp.dialog.NewFriendDialog
import com.example.brockapp.adapter.SuggestionsAdapter
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import com.amazonaws.regions.Regions
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import androidx.appcompat.app.AlertDialog
import com.example.brockapp.singleton.User
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import com.example.brockapp.viewmodel.UserViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsFragment: Fragment(R.layout.fragment_friends) {
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelFriends: FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User.getInstance()
        val db: BrockDB = BrockDB.getInstance(requireContext())

        val usernameTextView = view.findViewById<EditText>(R.id.search_user_text_area)
        val syncButton = view.findViewById<FloatingActionButton>(R.id.user_synchronized_button)

        val credentialsProvider = CognitoCachingCredentialsProvider(requireContext(), "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00", Regions.EU_WEST_3)
        val s3Client = AmazonS3Client(credentialsProvider)

        val viewModelFactory = FriendsViewModelFactory(s3Client, db, requireContext())
        viewModelFriends = ViewModelProvider(requireActivity(), viewModelFactory)[FriendsViewModel::class.java]

        val viewModelFactoryUser = UserViewModelFactory(db)
        viewModelUser = ViewModelProvider(requireActivity(), viewModelFactoryUser)[UserViewModel::class.java]

        observeFriends()
        observeSuggestion()
        observeAddedFriend()

        viewModelFriends.getCurrentFriends(user.id)

        syncButton.setOnClickListener {
            if (user.flag) {
                viewModelFriends.uploadUserData()
                syncButton.isEnabled = false
            } else {
                showShareDataDialog()
            }

            android.os.Handler().postDelayed({
                syncButton.isEnabled = true
            }, 5000)
        }

        usernameTextView.addTextChangedListener {
            val usernameToSearch = usernameTextView.text.toString()

            if (usernameToSearch.length > 2) {
                viewModelFriends.searchUser(usernameToSearch)
            } else {
                Log.d("FRIENDS_FRAGMENT", "Search with empty body not supported.")
            }
        }
    }

    private fun observeFriends() {
        viewModelFriends.friends.observe(viewLifecycleOwner) { friends ->
            if (!friends.isNullOrEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.friends_recycler_view)
                populateFriendsRecyclerView(friends, recyclerView)
            }
        }
    }

    private fun observeSuggestion() {
        viewModelFriends.suggestions.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isNotEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.suggestions_recycler_view)
                populateSuggestionsRecyclerView(suggestions, recyclerView)
            }
        }
    }

    private fun observeAddedFriend() {
        viewModelFriends.errorAddFriend.observe(viewLifecycleOwner) { errorAddFriend ->
            if (errorAddFriend) {
                Log.d("FRIENDS_FRAGMENT", "Amico aggiunto alla lista.")
            } else {
                Toast.makeText(context, "Amico già presente nella lista.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFriendsRecyclerView(friends: List<String>, friendsRecyclerView: RecyclerView?) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val friendsAdapter = FriendsAdapter(friends) { friend ->
            CoroutineScope(Dispatchers.Main).launch {
                val friendData = withContext(Dispatchers.IO) {
                    viewModelFriends.loadFriendData(friend)
                }

                friendData?.let { showFriendActivity(it) }
            }
        }

        friendsRecyclerView?.adapter = friendsAdapter
        friendsRecyclerView?.layoutManager = layoutManager
    }

    private fun populateSuggestionsRecyclerView(usernames: List<String>, suggestionsRecyclerView: RecyclerView?) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = SuggestionsAdapter(usernames) { username ->
            showNewFriendDialog(username, viewModelFriends)
        }

        suggestionsRecyclerView?.layoutManager = layoutManager
        suggestionsRecyclerView?.adapter = adapter
    }

    private fun showShareDataDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_share_data)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                User.flag = true
                viewModelUser.changeSharingDataFlag(User.username, User.password)
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showNewFriendDialog(username: String, viewModel: FriendsViewModel) {
        activity?.let {
            NewFriendDialog(username, viewModel).show(it.supportFragmentManager, "CUSTOM_NEW_FRIEND_DIALOG")
        }
    }

    // Da rendere dialog migliore.
    private fun showFriendActivity(friend: Friend) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_friend_data, null)

        val friendDataTextView: TextView = dialogView.findViewById(R.id.friend_data_text_view)
        val closeButton: Button = dialogView.findViewById(R.id.close_button)

        val friendData = StringBuilder()
        friendData.append("Username: ${friend.username}\n\n")

        friendData.append("------CAMMINATA------\n")
        friend.walkActivities.forEach { activity ->


            if(activity.transitionType == 0)
                friendData.append("Iniziata alle: ${activity.timestamp}\n")
            else
                friendData.append("Terminata alle: ${activity.timestamp}, passi fatti: ${activity.stepNumber}\n")

        }

        friendData.append("------VEICOLO------\n")
        friend.vehicleActivities.forEach { activity ->
            if(activity.transitionType == 0)
                friendData.append("Iniziata alle: ${activity.timestamp}\n")
            else
                friendData.append("Terminata alle: ${activity.timestamp}, distanza percorsa: ${activity.distanceTravelled}\n")

        }

        friendData.append("------FERMO------\n")
        friend.stillActivities.forEach { activity ->
            if(activity.transitionType == 0)
                friendData.append("Iniziata alle: ${activity.timestamp}\n")
            else
                friendData.append("Terminata alle: ${activity.timestamp}\n")
        }

        friendDataTextView.text = friendData.toString()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}