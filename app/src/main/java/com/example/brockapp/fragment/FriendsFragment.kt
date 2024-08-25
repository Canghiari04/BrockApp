package com.example.brockapp.fragment

import FriendsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.example.brockapp.R
import com.example.brockapp.adapter.UsersAdapter
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.FriendEntity
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.brockapp.singleton.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsFragment : Fragment(R.layout.friends_fragment) {
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var syncFriendsDataButton: FloatingActionButton
    private lateinit var searchUserButton: Button
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var friends: List<FriendEntity>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db: BrockDB = BrockDB.getInstance(requireContext())
        val credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )
        val s3Client = AmazonS3Client(credentialsProvider)

        CoroutineScope(Dispatchers.IO).launch {
            friends = db.FriendDao().getFriendsByUserId(User.id)
            withContext(Dispatchers.Main) {
                val friendsViewModelFactory = FriendsViewModelFactory(s3Client, db, requireContext(), friends)
                friendsViewModel = ViewModelProvider(requireActivity(), friendsViewModelFactory)[FriendsViewModel::class.java]

                observeFriendsUsername()
                observeOtherUsers()
            }
        }

        syncFriendsDataButton = view.findViewById(R.id.friends_synchronized_button)
        searchUserButton = view.findViewById(R.id.search_user_button)
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        friendsRecyclerView.layoutManager = LinearLayoutManager(context)

        usersRecyclerView = view.findViewById(R.id.users_recycler_view)
        usersRecyclerView.layoutManager = LinearLayoutManager(context)

        syncFriendsDataButton.setOnClickListener {
            friendsViewModel.uploadUserData()

            syncFriendsDataButton.isEnabled = false
            android.os.Handler().postDelayed({
                syncFriendsDataButton.isEnabled = true
            }, 5000)
        }

        searchUserButton.setOnClickListener {
            val usernameToSearch = view.findViewById<EditText>(R.id.search_user_text_area).text.toString()
            friendsViewModel.searchUser(usernameToSearch)
        }
    }

    private fun showFriendActivity(friend: Friend) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.friend_data, null)

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

    private fun observeOtherUsers() {
        friendsViewModel.newUser.observe(viewLifecycleOwner) { username ->
            if (username.isNotEmpty()) {
                populateUsersRecyclerView(username)
            }
        }
    }

    private fun observeFriendsUsername() {
        friendsViewModel.friendsUsername.observe(viewLifecycleOwner) { friends ->
            if (friends.isNotEmpty()) {
                populateFriendsRecyclerView(friends)
            }
        }
    }

    private fun populateUsersRecyclerView(username: String) {
        val adapter = UsersAdapter(username, friendsViewModel)
        usersRecyclerView.adapter = adapter
    }

    private fun populateFriendsRecyclerView(friends: List<String>) {
        friendsAdapter = FriendsAdapter(friends, friendsViewModel) { friend ->
            CoroutineScope(Dispatchers.Main).launch {
                val friendData = withContext(Dispatchers.IO) {
                    friendsViewModel.loadFriendData(friend)
                }
                friendData?.let { showFriendActivity(it) }
            }
        }
        friendsRecyclerView.adapter = friendsAdapter
    }

}
