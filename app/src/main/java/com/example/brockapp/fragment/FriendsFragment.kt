package com.example.brockapp.fragment

import FriendsAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.FriendsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsFragment: Fragment(R.layout.friends_fragment) {
    private lateinit var s3Client: AmazonS3Client
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var syncDataFriendsButton: FloatingActionButton
    private lateinit var credentialsProvider: CognitoCachingCredentialsProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )
        s3Client = AmazonS3Client(credentialsProvider)


        syncDataFriendsButton = view.findViewById(R.id.friends_synchronized_button)

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        friendsRecyclerView.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            syncDataFriendsButton.setOnClickListener {
                val db : BrockDB = BrockDB.getInstance(requireContext())
                val viewModel = FriendsViewModel(s3Client, db, requireContext())
                viewModel.uploadUserData()
                //viewModel.updateFriendsData()

                //observeFriends()
            }
        }
    }

//    private fun observeFriends() {
//        friendsViewModel.friends.observe(viewLifecycleOwner) { friends ->
//            if (friends.isNotEmpty()) {
//                populateRecyclerView(friends)
//            }
//        }
//    }

    private fun populateRecyclerView(friends: List<Friend>) {
        val adapter = FriendsAdapter(friends)
        friendsRecyclerView.adapter = adapter
    }
}
