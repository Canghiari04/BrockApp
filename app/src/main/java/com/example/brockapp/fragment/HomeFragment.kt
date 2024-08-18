package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.adapter.HomeAdapter
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class HomeFragment : Fragment(R.layout.home_fragment) {
    private lateinit var user: User
    private lateinit var db: BrockDB
    private lateinit var viewModel: ActivitiesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler_view)

        db = BrockDB.getInstance(requireContext())
        val factoryViewModelActivities = ActivitiesViewModelFactory(db)

        viewModel = ViewModelProvider(this, factoryViewModelActivities)[ActivitiesViewModel::class.java]

        user = User.getInstance()
        viewModel.getWeekUserActivities(LocalDate.now(), user)

        viewModel.sortedWeekActivitiesDayList.observe(viewLifecycleOwner) { item ->
            if (item.isNotEmpty()) {
                populateHomeRecyclerView(recyclerView, item)
            }
        }
    }

    private fun populateHomeRecyclerView(homeList: RecyclerView, activities: List<UserActivity>) {
        val adapterHome = HomeAdapter(activities)
        val layoutManager = LinearLayoutManager(requireContext())

        homeList.adapter = adapterHome
        homeList.layoutManager = layoutManager
    }
}