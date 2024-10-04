package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.adapter.ActivityAdapter
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.util.Log
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class ActivityPage: Fragment(R.layout.page_activities) {
    private var rangeUtil = PeriodRangeImpl()

    // The spinner's items are initially set to the first element of their array string
    private var selectedActivityType = "Still"
    private var selectedTimeRange = rangeUtil.getDayRange(LocalDate.now())

    private lateinit var spinnerType: Spinner
    private lateinit var spinnerRange: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ActivitiesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_page_activities)

        spinnerType = view.findViewById(R.id.spinner_type_activity)
        setUpSpinnerActivity(spinnerType)

        spinnerRange = view.findViewById(R.id.spinner_range_activity)
        setUpSpinnerRange(spinnerRange)

        val db = BrockDB.getInstance(requireContext())
        val factoryViewModel = ActivitiesViewModelFactory(db)
        viewModel = ViewModelProvider(this, factoryViewModel)[ActivitiesViewModel::class.java]

        observeUserActivities()
    }

    private fun setUpSpinnerActivity(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_activity)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedActivityType = spinnerItems[position]
                viewModel.getUserActivities(selectedTimeRange.first, selectedTimeRange.second)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun setUpSpinnerRange(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_pie_chart_items)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTimeRange = when (spinnerItems[position]) {
                    "Day" -> rangeUtil.getDayRange(LocalDate.now())
                    "Week" -> rangeUtil.getWeekRange(LocalDate.now())
                    "Month" -> rangeUtil.getMonthRange(LocalDate.now())
                    else -> rangeUtil.getDayRange(LocalDate.now())
                }

                viewModel.getUserActivities(selectedTimeRange.first, selectedTimeRange.second)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun observeUserActivities() {
        viewModel.listActivities.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val filteredList = it.filter { activities ->
                    when (selectedActivityType) {
                        "Still" -> {
                            activities.type == "Still"
                        }

                        "Vehicle" -> {
                            activities.type == "Vehicle"
                        }

                        "Walk" -> {
                            activities.type == "Walk"
                        }

                        "All" -> {
                            true
                        }

                        else -> {
                            true
                        }
                    }
                }

                populateRecyclerView(recyclerView, filteredList)
            } else {
                Log.d("ACTIVITY_PAGE", "No one activities detected")
            }
        }
    }

    private fun populateRecyclerView(recyclerView: RecyclerView, list: List<UserActivity>) {
        val adapter = ActivityAdapter(list)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }
}