package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.adapter.DailyActivityAdapter
import com.example.brockapp.viewmodel.FriendsViewModelFactory

import java.io.File
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class VehicleFragment: Fragment(R.layout.fragment_vehicle) {
    private lateinit var viewModel: FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val s3Client = S3ClientProvider.getInstance(requireContext())

        val file = File(requireContext().filesDir, "user_data.json")

        val db = BrockDB.getInstance(requireContext())
        val viewModelFactory = FriendsViewModelFactory(s3Client, db, file)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FriendsViewModel::class.java]

        observeFriendVehicleActivities()
    }

    private fun observeFriendVehicleActivities() {
        viewModel.friendVehicleActivities.observe(viewLifecycleOwner) { friendVehicleActivities ->
            val adapter = DailyActivityAdapter(friendVehicleActivities)
            val layoutManager = LinearLayoutManager(requireContext())

            val recyclerView = view?.findViewById<RecyclerView>(R.id.vehicle_recycler_view)
            recyclerView?.adapter = adapter
            recyclerView?.layoutManager = layoutManager
        }
    }
}