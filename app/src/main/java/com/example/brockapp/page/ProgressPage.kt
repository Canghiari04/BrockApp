package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.viewmodel.GroupViewModelFactory
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.brockapp.util.ChartUtil
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart

abstract class ProgressPage: Fragment(R.layout.page_progress) {
    private var rangeUtil = PeriodRangeImpl()

    private var barChartMapper = mapOf(
        "Vehicle" to ::showVehicleBarChart,
        "Still" to ::showStillBarChart,
        "Walk" to ::showWalkBarChart
    )

    private var lineChartMapper = mapOf(
        "Vehicle" to ::showVehicleLineChart,
        "Walk" to ::showWalkLineChart
    )

    private var pieChartMapper = mapOf(
        "Day" to ::showDailyPieChart,
        "Week" to ::showWeeklyPieChart,
        "Month" to ::showMonthlyPieChart
    )

    protected var chartUtil = ChartUtil()

    // Text view
    private lateinit var titleThirdCardView: TextView

    // Table
    protected lateinit var infoFirstColumn: TextView
    protected lateinit var infoSecondColumn: TextView
    protected lateinit var titleSecondColumn: TextView

    // Bar charts
    protected lateinit var walkBarChart: BarChart
    protected lateinit var stillBarChart: BarChart
    protected lateinit var vehicleBarChart: BarChart

    // Line charts
    protected lateinit var runLineChart: LineChart
    protected lateinit var walkLineChart: LineChart
    protected lateinit var vehicleLineChart: LineChart

    // Pie chart
    protected lateinit var pieChart: PieChart

    // View model
    protected lateinit var groupViewModel: GroupViewModel
    protected lateinit var activitiesViewModel: ActivitiesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_welcome_progress).text =
            ("Welcome, " + MyUser.username + "! In this area you can check your progress done during the activities registered")

        showWelcomeCardView(view.findViewById(R.id.card_view_welcome_progress_page))

        titleThirdCardView = view.findViewById(R.id.text_view_title_third_card)

        // Table view
        titleSecondColumn = view.findViewById(R.id.text_view_title_second_column)
        infoFirstColumn = view.findViewById(R.id.text_view_content_first_column)
        infoSecondColumn = view.findViewById(R.id.text_view_content_second_column)

        // Bar charts
        walkBarChart = view.findViewById(R.id.bar_chart_walk)
        stillBarChart = view.findViewById(R.id.bar_chart_still)
        vehicleBarChart = view.findViewById(R.id.bar_chart_vehicle)

        // Line charts
        runLineChart = view.findViewById(R.id.line_chart_run)
        walkLineChart = view.findViewById(R.id.line_chart_walk)
        vehicleLineChart = view.findViewById(R.id.line_chart_vehicle)

        // Pie chart
        pieChart = view.findViewById(R.id.pie_chart_activities)

        setUpBarChartSpinner(view.findViewById(R.id.spinner_bar_chart))
        setUpLineChartSpinner(view.findViewById(R.id.spinner_line_chart))
        setUpPieChartSpinner(view.findViewById(R.id.spinner_pie_chart))

        val db = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val groupViewModelFactory = GroupViewModelFactory(s3Client, db)
        groupViewModel = ViewModelProvider(this, groupViewModelFactory)[GroupViewModel::class.java]

        val activitiesFactoryViewModel = ActivitiesViewModelFactory(db)
        activitiesViewModel = ViewModelProvider(this, activitiesFactoryViewModel)[ActivitiesViewModel::class.java]

        observeVehicleTimeSpent()
        observeUserKilometers()
        observeVehicleBarChartEntries()

        observeStillTimeSpent()
        observeStillBarChartEntries()

        observeWalkTimeSpent()
        observeUserSteps()
        observeWalkBarChartEntries()

        observeVehicleLineChartEntries()
        observeWalkLineChartEntries()

        observeUserActivities()
    }

    protected abstract fun showWelcomeCardView(cardView: CardView)

    private fun setUpBarChartSpinner(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_bar_chart)

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

                barChartMapper[itemSelected]?.invoke(range)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun setUpLineChartSpinner(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_line_chart)

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
                val range = rangeUtil.getWeekRange(LocalDate.now())

                lineChartMapper[itemSelected]?.invoke(range)
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
                pieChartMapper[itemSelected]?.invoke()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    protected abstract fun observeVehicleTimeSpent()

    protected abstract fun observeUserKilometers()

    protected abstract fun observeVehicleBarChartEntries()

    protected abstract fun observeStillTimeSpent()

    protected abstract fun observeStillBarChartEntries()

    protected abstract fun observeWalkTimeSpent()

    protected abstract fun observeUserSteps()

    protected abstract fun observeWalkBarChartEntries()

    protected abstract fun observeVehicleLineChartEntries()

    protected abstract fun observeWalkLineChartEntries()

    protected abstract fun observeUserActivities()

    private fun showVehicleBarChart(range: Pair<String, String>) {
        walkBarChart.visibility = View.GONE
        stillBarChart.visibility = View.GONE

        vehicleBarChart.visibility = View.VISIBLE

        infoSecondColumn.visibility = View.VISIBLE
        titleSecondColumn.visibility = View.VISIBLE

        loadVehicleTime(range.first, range.second)
        loadKilometers(range.first, range.second)
        defineVehicleBarChartEntries(range.first, range.second)
    }

    protected abstract fun loadVehicleTime(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun loadKilometers(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun defineVehicleBarChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showStillBarChart(range: Pair<String, String>) {
        walkBarChart.visibility = View.GONE
        vehicleBarChart.visibility = View.GONE

        stillBarChart.visibility = View.VISIBLE

        infoSecondColumn.visibility = View.GONE
        titleSecondColumn.visibility = View.GONE

        loadStillTime(range.first, range.second)
        defineStillBarChartEntries(range.first, range.second)
    }

    protected abstract fun loadStillTime(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun defineStillBarChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showWalkBarChart(range: Pair<String, String>) {
        stillBarChart.visibility = View.GONE
        vehicleBarChart.visibility = View.GONE

        walkBarChart.visibility = View.VISIBLE

        infoSecondColumn.visibility = View.VISIBLE
        titleSecondColumn.visibility = View.VISIBLE

        loadWalkTime(range.first, range.second)
        loadStepNumber(range.first, range.second)
        defineWalkBarChartEntries(range.first, range.second)
    }

    protected abstract fun loadWalkTime(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun loadStepNumber(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun defineWalkBarChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showVehicleLineChart(range: Pair<String, String>) {
        runLineChart.visibility = View.GONE
        walkLineChart.visibility = View.GONE

        vehicleLineChart.visibility = View.VISIBLE

        defineVehicleLineChartEntries(range.first, range.second)
    }

    protected abstract fun defineVehicleLineChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showWalkLineChart(range: Pair<String, String>) {
        runLineChart.visibility = View.GONE
        vehicleLineChart.visibility = View.GONE

        walkLineChart.visibility = View.VISIBLE

        defineWalkLineChartEntries(range.first, range.second)
    }

    protected abstract fun defineWalkLineChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showDailyPieChart() {
        titleThirdCardView.setText("This day")

        val range = rangeUtil.getDayRange(LocalDate.now())
        countActivities(range.first, range.second)
    }

    private fun showWeeklyPieChart() {
        titleThirdCardView.setText("This week")

        val range = rangeUtil.getWeekRange(LocalDate.now())
        countActivities(range.first, range.second)
    }

    private fun showMonthlyPieChart() {
        titleThirdCardView.setText("This month")

        val range = rangeUtil.getMonthRange(LocalDate.now())
        countActivities(range.first, range.second)
    }

    protected abstract fun countActivities(startOfPeriod: String, endOfPeriod: String)
}