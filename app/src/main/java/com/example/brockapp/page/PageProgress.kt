package com.example.brockapp.page

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.util.Log
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import java.time.YearMonth
import android.widget.Toast
import android.widget.Spinner
import android.graphics.Color
import android.widget.TextView
import android.graphics.Typeface
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class PageProgress: Fragment(R.layout.page_progress) {
    private var rangeUtil = PeriodRangeImpl()

    private lateinit var pieChart: PieChart
    private lateinit var walkBarChart: BarChart
    private lateinit var vehicleBarChart: BarChart
    private lateinit var titleSecondCard: TextView
    private lateinit var titleFirstColumn: TextView
    private lateinit var contentFirstColumn: TextView
    private lateinit var contentSecondColumn: TextView
    private lateinit var viewModel: ActivitiesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_welcome_progress)
            .setText("Welcome, " + User.username + "! In this area you can check your progress done during the activities registered")

        titleSecondCard = view.findViewById(R.id.text_view_title_second_card)

        titleFirstColumn = view.findViewById(R.id.text_view_title_first_column)

        contentFirstColumn = view.findViewById(R.id.text_view_content_first_column)
        contentSecondColumn = view.findViewById(R.id.text_view_content_second_column)

        pieChart = view.findViewById(R.id.pie_char_activities)
        walkBarChart = view.findViewById(R.id.bar_chart_walk)
        vehicleBarChart = view.findViewById(R.id.bar_chart_vehicle)

        val barChartSpinner = view.findViewById<Spinner>(R.id.spinner_bar_chart)
        setUpBarChartSpinner(barChartSpinner)

        val pieChartSpinner = view.findViewById<Spinner>(R.id.spinner_pie_chart)
        setUpPieChartSpinner(pieChartSpinner)

        val db = BrockDB.getInstance(requireContext())
        val factoryViewModel = ActivitiesViewModelFactory(db)
        viewModel = ViewModelProvider(this, factoryViewModel)[ActivitiesViewModel::class.java]

        observeUserActivities()

        observeUserKilometers()
        observeVehicleTimeSpent()
        observeVehicleActivities()

        observeUserSteps()
        observeWalkTimeSpent()
        observeWalkActivities()
    }

    private fun setUpBarChartSpinner(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_bar_chart_items)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val itemSelected = spinnerItems[position]
                val range = rangeUtil.getWeekRange(LocalDate.now())

                when (itemSelected) {
                    "Vehicle" -> {
                        walkBarChart.visibility = View.GONE
                        vehicleBarChart.visibility = View.VISIBLE

                        titleFirstColumn.setText("Distance")
                        contentFirstColumn.setText("0 km")

                        viewModel.getVehicleActivities(range.first, range.second)
                        viewModel.getKilometers(range.first, range.second)
                        viewModel.getVehicleTime(range.first, range.second)
                    }

                    "Walk" -> {
                        vehicleBarChart.visibility = View.GONE
                        walkBarChart.visibility = View.VISIBLE

                        titleFirstColumn.setText("Steps")
                        contentFirstColumn.setText("0 steps")

                        viewModel.getWalkActivities(range.first, range.second)
                        viewModel.getSteps(range.first, range.second)
                        viewModel.getWalkTime(range.first, range.second)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun setUpPieChartSpinner(spinner: Spinner?) {
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
                val itemSelected = spinnerItems[position]

                val range: Pair<String, String>

                when (itemSelected) {
                    "Day" -> {
                        titleSecondCard.setText("This day")

                        range = rangeUtil.getDayRange(LocalDate.now())
                        viewModel.getCountsOfActivities(range.first, range.second)
                    }

                    "Week" -> {
                        titleSecondCard.setText("This week")

                        range = rangeUtil.getWeekRange(LocalDate.now())
                        viewModel.getCountsOfActivities(range.first, range.second)
                    }

                    "Month" -> {
                        titleSecondCard.setText("This month")

                        range = rangeUtil.getMonthRange(LocalDate.now())
                        viewModel.getCountsOfActivities(range.first, range.second)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun observeUserActivities() {
        viewModel.mapCountActivities.observe(viewLifecycleOwner) { activities ->
            if (!activities.isNullOrEmpty()) {
                setupActivityTypePieChart(activities)
            } else {
                Log.d("PAGE_PROGRESS", "None activity done in this range")
            }
        }
    }

    private fun setupActivityTypePieChart(activities: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()

        activities.forEach { (activityType, value) ->
            if (value > 0) {
                val label = when (activityType) {
                    STILL_ACTIVITY_TYPE -> "Still"
                    VEHICLE_ACTIVITY_TYPE -> "Vehicle"
                    WALK_ACTIVITY_TYPE -> "Walk"
                    else -> "Unknown"
                }
                entries.add(PieEntry(value.toFloat(), label))
            }
        }

        if (entries.isNotEmpty()) {
            val redGradientColors = listOf(
                Color.parseColor("#D32F2F"),
                Color.parseColor("#B71C1C"),
                Color.parseColor("#FF1744")
            )

            val dataSet = PieDataSet(entries, " ").apply {
                colors = redGradientColors
                valueTextSize = 12f
                valueTypeface = Typeface.DEFAULT_BOLD
            }

            val data = PieData(dataSet)
            pieChart.data = data

            pieChart.legend.xEntrySpace = 16f
            pieChart.description?.isEnabled = false

            pieChart.invalidate()
            pieChart.setUsePercentValues(true)
            pieChart.setDrawEntryLabels(false)

            pieChart.visibility = View.VISIBLE
        } else {
            pieChart.visibility = View.GONE
        }
    }

    private fun observeUserKilometers() {
        viewModel.meters.observe(viewLifecycleOwner) { meters ->
            if (meters != 0) {
                val kilometers = (meters/1000)
                contentFirstColumn.setText(kilometers.toString() + " km")
            } else {
                Log.d("PAGE_PROGRESS", "None vehicle activity detect")
            }
        }
    }

    private fun observeVehicleTimeSpent() {
        viewModel.vehicleTime.observe(viewLifecycleOwner) { time ->
            val minute = (time / 60).toInt()
            contentSecondColumn.setText(minute.toString() + "m")
        }
    }

    private fun observeVehicleActivities() {
        viewModel.listVehicleActivities.observe(viewLifecycleOwner) { activities ->
            if (!activities.isNullOrEmpty()) {
                setUpVehicleBarChart(activities)
            } else {
                Log.d("PAGE_PROGRESS", "None vehicle activity detect")
            }
        }
    }

    private fun setUpVehicleBarChart(activities: List<UserVehicleActivityEntity>?) {
        val entries = ArrayList<BarEntry>()
        val yearMonth = YearMonth.of(LocalDate.now().year, LocalDate.now().month)

        val distancePerDay = activities?.groupBy {
            it.timestamp?.let { timestamp ->
                LocalDate.parse(timestamp, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)).dayOfMonth
            } ?: 0
        }?.mapValues { entry ->
            entry.value.sumOf { it.distanceTravelled ?: 0.0 }
        }

        for (day in 1..yearMonth.lengthOfMonth()) {
            val totalDistanceTravelled = distancePerDay?.get(day) ?: 0.0
            entries.add(BarEntry(day.toFloat(), totalDistanceTravelled.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Distance traveled")
        dataSet.color = Color.parseColor("#BB2222")

        val data = BarData(dataSet)
        data.setDrawValues(false)
        vehicleBarChart.data = data

        vehicleBarChart.legend.isEnabled = false
        vehicleBarChart.description.isEnabled = false

        vehicleBarChart.xAxis.setDrawGridLines(false)
        vehicleBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        vehicleBarChart.xAxis.valueFormatter = IndexAxisValueFormatter((1..yearMonth.lengthOfMonth()).map { it.toString() })

        vehicleBarChart.axisLeft.axisMinimum = 0f
        vehicleBarChart.axisRight.axisMinimum = 0f

        vehicleBarChart.animateY(500)

        vehicleBarChart.invalidate()
    }

    private fun observeUserSteps() {
        viewModel.steps.observe(viewLifecycleOwner) { steps ->
            if (steps != 0) {
                contentFirstColumn.setText(steps.toString() + " steps")
            } else {
                Log.d("PAGE_PROGRESS", "None walk activity detect")
            }
        }
    }

    private fun observeWalkTimeSpent() {
        viewModel.walkTime.observe(viewLifecycleOwner) { time ->
            val minute = (time / 60).toInt()
            contentSecondColumn.setText(minute.toString() + "m")
        }
    }

    private fun observeWalkActivities() {
        viewModel.listWalkActivities.observe(viewLifecycleOwner) { activities ->
            if (!activities.isNullOrEmpty()) {
                setUpWalkBarChart(activities)
            } else {
                Log.d("PAGE_PROGRESS", "None vehicle activity detect")
            }
        }
    }

    private fun setUpWalkBarChart(activities: List<UserWalkActivityEntity>) {
        val entries = ArrayList<BarEntry>()
        val yearMonth = YearMonth.of(LocalDate.now().year, LocalDate.now().month)

        val stepsPerDay = activities.groupBy {
            it.timestamp?.let { timestamp ->
                LocalDate.parse(timestamp, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)).dayOfMonth
            } ?: 0
        }.mapValues { entry ->
            entry.value.sumOf { it.stepNumber }
        }

        for (day in 1..yearMonth.lengthOfMonth()) {
            val totalSteps = stepsPerDay[day] ?: 0f
            entries.add(BarEntry(day.toFloat(), totalSteps.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Step number")
        dataSet.color = Color.parseColor("#BB2222")

        val data = BarData(dataSet)
        walkBarChart.data = data

        walkBarChart.legend.isEnabled = false
        walkBarChart.description.isEnabled = false

        walkBarChart.xAxis.setDrawGridLines(false)
        walkBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        walkBarChart.xAxis.valueFormatter = IndexAxisValueFormatter((1..yearMonth.lengthOfMonth()).map { it.toString() })

        walkBarChart.axisLeft.axisMinimum = 0f
        walkBarChart.axisRight.axisMinimum = 0f

        walkBarChart.animateY(500)

        walkBarChart.invalidate()
    }
}