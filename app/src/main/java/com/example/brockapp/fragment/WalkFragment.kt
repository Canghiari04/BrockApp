package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.adapter.DailyActivityAdapter
import com.example.brockapp.viewmodel.FriendsViewModelFactory

import android.os.Bundle
import android.view.View
import com.amazonaws.regions.Regions
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.CognitoCachingCredentialsProvider

class WalkFragment: Fragment(R.layout.fragment_walk) {
    private lateinit var viewModel: FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val credentialsProvider = CognitoCachingCredentialsProvider(requireContext(), "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00", Regions.EU_WEST_3)
        val s3Client = AmazonS3Client(credentialsProvider)

        val db = BrockDB.getInstance(requireContext())
        val viewModelFactory = FriendsViewModelFactory(s3Client, db, requireContext())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FriendsViewModel::class.java]

        observeFriendWalkActivities()
    }

    private fun observeFriendWalkActivities() {
        viewModel.friendWalkActivities.observe(viewLifecycleOwner) { friendWalkActivities ->
            val adapter = DailyActivityAdapter(friendWalkActivities)
            val layoutManager = LinearLayoutManager(requireContext())

            val recyclerView = view?.findViewById<RecyclerView>(R.id.vehicle_recycler_view)
            recyclerView?.adapter = adapter
            recyclerView?.layoutManager = layoutManager
        }
    }
}