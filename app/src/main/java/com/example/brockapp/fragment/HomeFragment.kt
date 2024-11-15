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
import java.time.DayOfWeek
import java.time.LocalDate
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
    private lateinit var selectedItem: String
    private lateinit var stepsTitle: TextView
    private lateinit var staticTitle: TextView
    private lateinit var stepsCountText: TextView
    private lateinit var staticCountText: TextView
    private lateinit var kilometersTitle: TextView
    private lateinit var stepsProgressBar: ProgressBar
    private lateinit var kilometersCountText: TextView
    private lateinit var staticProgressBar: ProgressBar
    private lateinit var viewModel: ActivitiesViewModel
    private lateinit var kilometersProgressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        staticTitle = view.findViewById(R.id.static_title_text_view)
        staticProgressBar = view.findViewById(R.id.static_progress_bar)
        staticCountText = view.findViewById(R.id.static_count_text)

        kilometersTitle = view.findViewById(R.id.kilometers_title_text_view)
        kilometersProgressBar = view.findViewById(R.id.kilometers_progress_bar)
        kilometersCountText = view.findViewById(R.id.kilometers_count_text)

        stepsTitle = view.findViewById(R.id.steps_title_text_view)
        stepsProgressBar = view.findViewById(R.id.steps_progress_bar)
        stepsCountText = view.findViewById(R.id.steps_count_text)

        val spinner = view.findViewById<Spinner>(R.id.home_spinner)
        setUpSpinner(spinner)

        val db = BrockDB.getInstance(requireContext())
        val factoryViewModel = ActivitiesViewModelFactory(db)
        viewModel = ViewModelProvider(this, factoryViewModel)[ActivitiesViewModel::class.java]

        observeUserActivities()
        observeUserStillTime()
        observeUserKilometers()
        observeUserSteps()
    }

    private fun setUpSpinner(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_items)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedItem = spinnerItems[position]
                val range: Pair<String, String>

                when (selectedItem) {
                    "Giorno" -> {
                        range = getDayRange(LocalDate.now())

                        staticTitle.setText(R.string.daily_static_text)
                        kilometersTitle.setText(R.string.daily_kilometers_text)
                        stepsTitle.setText(R.string.daily_step_text)

                        staticProgressBar.max = 24
                        kilometersProgressBar.max = 100000
                        stepsProgressBar.max = 10000
                    }

                    "Settimana" -> {
                        range = getWeekRange(LocalDate.now())

                        staticTitle.setText(R.string.weekly_static_text)
                        kilometersTitle.setText(R.string.weekly_kilometers_text)
                        stepsTitle.setText(R.string.weekly_step_text)

                        staticProgressBar.max = 24 * 7
                        kilometersProgressBar.max = 100000 * 7
                        stepsProgressBar.max = 10000 * 7
                    }

                    else -> {
                        range = getDayRange(LocalDate.now())

                        staticTitle.setText(R.string.daily_static_text)
                        kilometersTitle.setText(R.string.daily_kilometers_text)
                        stepsTitle.setText(R.string.daily_step_text)

                        staticProgressBar.max = 86400
                        kilometersProgressBar.max = 100000
                        stepsProgressBar.max = 10000
                    }
                }

                viewModel.getUserActivities(range.first, range.second)
                viewModel.getStillTime(range.first, range.second)
                viewModel.getKilometers(range.first, range.second)
                viewModel.getSteps(range.first, range.second)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //
            }
        }
    }

    private fun observeUserActivities() {
        viewModel.listExitActivities.observe(viewLifecycleOwner) { listExitActivities ->
            if (listExitActivities.isNotEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.home_recycler_view)
                populateHomeRecyclerView(recyclerView, listExitActivities)
            } else {
                Log.d("HOME_FRAGMENT", "None activities.")
            }
        }
    }

    private fun observeUserStillTime() {
        viewModel.stillTime.observe(viewLifecycleOwner) { stillTime ->
            val timeSpentInHour = (stillTime!!.toInt() / 60 / 60)
            staticProgressBar.progress = timeSpentInHour

            if (selectedItem == "Giorno") {
                staticCountText.setText("$timeSpentInHour/24 ore")
            } else {
                staticCountText.setText("$timeSpentInHour/168 ore")
            }
        }
    }

    private fun observeUserKilometers() {
        viewModel.meters.observe(viewLifecycleOwner) { meters ->
            kilometersProgressBar.progress = meters

            val kilometers = meters / 1000

            if(selectedItem == "Giorno") {
                kilometersCountText.setText("$kilometers/100 km")
            } else {
                kilometersCountText.setText("$kilometers/700 km")
            }
        }
    }

    private fun observeUserSteps() {
        viewModel.steps.observe(viewLifecycleOwner) { steps ->
            stepsProgressBar.progress = steps

            if (selectedItem == "Giorno") {
                stepsCountText.setText("$steps/10000 passi")
            } else {
                stepsCountText.setText("$steps/70000 passi")
            }
        }
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

    private fun populateHomeRecyclerView(recyclerView: RecyclerView?, activities: List<UserActivity>) {
        val adapterHome = HomeAdapter(activities)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView?.adapter = adapterHome
        recyclerView?.layoutManager = layoutManager
    }
}