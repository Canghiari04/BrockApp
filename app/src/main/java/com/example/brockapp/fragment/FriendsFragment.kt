package com.example.brockapp.fragment

import FriendsAdapter
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
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

class FriendsFragment: Fragment(R.layout.friends_fragment) {
    private lateinit var s3Client: AmazonS3Client
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var syncFriendsDataButton: FloatingActionButton
    private lateinit var searchUserButton : Button
    private lateinit var credentialsProvider: CognitoCachingCredentialsProvider
    private lateinit var friendsViewModel : FriendsViewModel

    private lateinit var friends: List<FriendEntity>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )
        s3Client = AmazonS3Client(credentialsProvider)
        val db : BrockDB = BrockDB.getInstance(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            friends = db.FriendDao().getFriendsByUserId(User.id)
            withContext(Dispatchers.Main){

            val friendsViewModelFactory = FriendsViewModelFactory(s3Client, db, requireContext(), friends)
            friendsViewModel = ViewModelProvider(requireActivity(), friendsViewModelFactory)[FriendsViewModel::class.java]

                observeFriends()
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

            syncFriendsDataButton.setEnabled(false)

            android.os.Handler().postDelayed( {
                syncFriendsDataButton.setEnabled(true)
            }, 5000)
        }

        searchUserButton.setOnClickListener {

            val usernameToSearch = view.findViewById<EditText>(R.id.search_user_text_area).text.toString()
            friendsViewModel.searchUser(usernameToSearch)
        }


    }

    private fun observeOtherUsers() {
        friendsViewModel.newUser.observe(viewLifecycleOwner){ username ->
            if(username.isNotEmpty()){
                populateUsersRecyclerView(username, friendsViewModel)
            }
        }
    }


    /**
     * Popolo le card view con i dati aggiornati che arrivano dal view model
     */
    private fun observeFriends() {
        friendsViewModel.friends.observe(viewLifecycleOwner) { friends ->
            if (friends.isNotEmpty()) {
                populateFriendsRecyclerView(friends)
            }
        }
    }

    private fun populateUsersRecyclerView(user: String, viewModel: FriendsViewModel) {
        val adapter = UsersAdapter(user, viewModel)
        usersRecyclerView.adapter = adapter

    }

    private fun populateFriendsRecyclerView(friends: List<String>) {
        val adapter = FriendsAdapter(friends)
        friendsRecyclerView.adapter = adapter
    }
}
