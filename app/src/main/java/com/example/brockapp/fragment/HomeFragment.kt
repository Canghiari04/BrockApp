package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.adapter.HomeAdapter
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.util.Log
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import java.time.DayOfWeek
import android.widget.Spinner
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var user: User
    private lateinit var stepsCountText: TextView
    private lateinit var stepsProgressBar : ProgressBar
    private lateinit var viewModel: ActivitiesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepsProgressBar = view.findViewById(R.id.steps_progress_bar)
        stepsCountText = view.findViewById(R.id.steps_count_text)

        val spinner = view.findViewById<Spinner>(R.id.home_spinner)
        setUpSpinner(spinner)

        user = User.getInstance()

        val db = BrockDB.getInstance(requireContext())
        val factoryViewModelActivities = ActivitiesViewModelFactory(db)

        viewModel = ViewModelProvider(this, factoryViewModelActivities)[ActivitiesViewModel::class.java]

        observeUserActivities()
        observeUserSteps()
    }

    private fun setUpSpinner(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_items)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = spinnerItems[position]
                val range = when (selectedItem) {
                    "Giorno" -> {
                        getDayRange(LocalDate.now())
                    }

                    "Settimana" -> {
                        getWeekRange(LocalDate.now())
                    }

                    else -> {
                        getDayRange(LocalDate.now())
                    }
                }

                viewModel.getUserActivities(range.first, range.second, user)
                viewModel.getSteps(range.first, range.second, user)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //
            }
        }
    }

    private fun observeUserActivities() {
        viewModel.listActivities.observe(viewLifecycleOwner) { listActivities ->
            if (listActivities.isNotEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.home_recycler_view)
                populateHomeRecyclerView(recyclerView, listActivities)
            } else {
                Log.d("HOME_FRAGMENT", "None activities.")
            }
        }
    }

    private fun observeUserSteps() {
        viewModel.steps.observe(viewLifecycleOwner) { steps ->
            if (steps > 0) {
                stepsProgressBar.progress = steps
                stepsCountText.setText("$steps/10000 passi")
            } else {
                Log.d("HOME_FRAGMENT", "None steps detected.")
            }
        }
    }

    private fun populateHomeRecyclerView(recyclerView: RecyclerView?, activities: List<UserActivity>) {
        val adapterHome = HomeAdapter(activities)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView?.adapter = adapterHome
        recyclerView?.layoutManager = layoutManager
    }

    private fun getDayRange(day: LocalDate): Pair<String, String> {
        val startOfDay = day.atStartOfDay().withSecond(0)
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }

    private fun getWeekRange(day: LocalDate): Pair<String, String> {
        val firstDay = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay().withSecond(0)
        val lastDay = day.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atStartOfDay().plusDays(1).minusSeconds(1)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(firstDay.format(outputFormatter), lastDay.format(outputFormatter))
    }
}