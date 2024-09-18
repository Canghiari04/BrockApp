package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.adapter.FriendActivitiesAdapter
import com.example.brockapp.viewmodel.FriendsViewModelFactory

import java.io.File
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class ActivityFragment: Fragment(R.layout.fragment_walk) {
    private lateinit var viewModel: FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val s3Client = S3ClientProvider.getInstance(requireContext())

        val file = File(requireContext().filesDir, "user_data.json")

        val db = BrockDB.getInstance(requireContext())
        val viewModelFactory = FriendsViewModelFactory(s3Client, db, file)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FriendsViewModel::class.java]

        observeFriendActivities()
    }

    private fun observeFriendActivities() {
        viewModel.friendActivities.observe(viewLifecycleOwner) { friendActivities ->
            if (!friendActivities.isNullOrEmpty()) {
                val adapter = FriendActivitiesAdapter(friendActivities)
                val layoutManager = LinearLayoutManager(requireContext())

                val recycler = view?.findViewById<RecyclerView>(R.id.activities_recycler_view)
                recycler?.adapter = adapter
                recycler?.layoutManager = layoutManager
            } else {
                Log.d("ACTIVITY_FRAGMENT", "Friend does not have any activities")
            }
        }
    }
}