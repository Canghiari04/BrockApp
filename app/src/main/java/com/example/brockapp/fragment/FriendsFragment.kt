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
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsFragment: Fragment(R.layout.friends_fragment) {
    private lateinit var s3Client: AmazonS3Client
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var syncFriendsDataButton: FloatingActionButton
    private lateinit var searchUserButton : Button
    private lateinit var credentialsProvider: CognitoCachingCredentialsProvider
    private lateinit var friendsViewModel : FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )
        s3Client = AmazonS3Client(credentialsProvider)
        val db : BrockDB = BrockDB.getInstance(requireContext())
        val friendsViewModelFactory = FriendsViewModelFactory(s3Client, db, requireContext())
        friendsViewModel = ViewModelProvider(this, friendsViewModelFactory)[FriendsViewModel::class.java]


        syncFriendsDataButton = view.findViewById(R.id.friends_synchronized_button)
        searchUserButton = view.findViewById(R.id.search_user_button)

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        friendsRecyclerView.layoutManager = LinearLayoutManager(context)

        usersRecyclerView = view.findViewById(R.id.users_recycler_view)
        usersRecyclerView.layoutManager = LinearLayoutManager(context)

        /*
        1.chiamare metodo observe() per mettere in ascolto il fragment
        2. aggiornare dati view model
        3. La lista viene aggiornata e sveglia il viewModel che aggiorna la view
         */

        observeFriends()

        observeOtherUsers()

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
                populateUsersRecyclerView(username)
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

    private fun populateUsersRecyclerView(user: String) {
        val adapter = UsersAdapter(user)
        usersRecyclerView.adapter = adapter

    }

    private fun populateFriendsRecyclerView(friends: List<Friend>) {
        val adapter = FriendsAdapter(friends)
        friendsRecyclerView.adapter = adapter
    }
}
