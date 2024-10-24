package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.util.ChartUtil
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewmodel.GroupViewModelFactory
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart

abstract class ProgressPage: Fragment(R.layout.page_progress) {
    private var rangeUtil = PeriodRangeImpl()

    private var barChartMapper = mapOf(
        "Vehicle" to ::showVehicleBarChart,
        "Run" to ::showRunBarChart,
        "Still" to ::showStillBarChart,
        "Walk" to ::showWalkBarChart
    )

    private var lineChartMapper = mapOf(
        "Vehicle" to ::showVehicleLineChart,
        "Run" to ::showRunLineChart,
        "Walk" to ::showWalkLineChart
    )

    private var pieChartMapper = mapOf(
        "Day" to ::showDailyPieChart,
        "Week" to ::showWeeklyPieChart,
        "Month" to ::showMonthlyPieChart
    )

    protected val chartUtil = ChartUtil()
    protected val toastUtil = ShowCustomToastImpl()

    protected lateinit var buttonUser: Button
    private lateinit var titleThirdCardView: TextView
    protected lateinit var cardViewYouProgressPage: CardView
    protected lateinit var cardViewUserProgressPage: CardView

    // Table
    protected lateinit var infoFirstColumn: TextView
    protected lateinit var infoSecondColumn: TextView
    protected lateinit var titleSecondColumn: TextView

    // Bar charts
    protected lateinit var runBarChart: BarChart
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
    protected lateinit var viewModelGroup: GroupViewModel
    protected lateinit var viewModelActivities: ActivitiesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_welcome_progress).text =
            ("Welcome, " + MyUser.username + "! In this area you can check your progress done")

        cardViewYouProgressPage = view.findViewById(R.id.card_view_welcome_you_progress_page)
        cardViewUserProgressPage = view.findViewById(R.id.card_view_welcome_user_progress_page)

        buttonUser = view.findViewById(R.id.button_user_progress_page)

        titleThirdCardView = view.findViewById(R.id.text_view_title_third_card)

        // Table view
        titleSecondColumn = view.findViewById(R.id.text_view_title_second_column)
        infoFirstColumn = view.findViewById(R.id.text_view_content_first_column)
        infoSecondColumn = view.findViewById(R.id.text_view_content_second_column)

        // Bar charts
        runBarChart = view.findViewById(R.id.bar_chart_run)
        walkBarChart = view.findViewById(R.id.bar_chart_walk)
        stillBarChart = view.findViewById(R.id.bar_chart_still)
        vehicleBarChart = view.findViewById(R.id.bar_chart_vehicle)

        // Line charts
        runLineChart = view.findViewById(R.id.line_chart_run)
        walkLineChart = view.findViewById(R.id.line_chart_walk)
        vehicleLineChart = view.findViewById(R.id.line_chart_vehicle)

        // Pie chart
        pieChart = view.findViewById(R.id.pie_chart_activities)

        val db = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val groupViewModelFactory = GroupViewModelFactory(s3Client, db)
        viewModelGroup = ViewModelProvider(this, groupViewModelFactory)[GroupViewModel::class.java]

        val activitiesFactoryViewModel = ActivitiesViewModelFactory(db)
        viewModelActivities = ViewModelProvider(this, activitiesFactoryViewModel)[ActivitiesViewModel::class.java]

        setUpCardView()

        setUpBarChartSpinner(view.findViewById(R.id.spinner_bar_chart))
        setUpLineChartSpinner(view.findViewById(R.id.spinner_line_chart))
        setUpPieChartSpinner(view.findViewById(R.id.spinner_pie_chart))

        observeVehicleTimeSpent()
        observeUserKilometersTravelled()
        observeVehicleBarChartEntries()

        observeRunTimeSpent()
        observeUserRunDistanceDone()
        observeRunBarChartEntries()

        observeStillTimeSpent()
        observeStillBarChartEntries()

        observeWalkTimeSpent()
        observeUserSteps()
        observeWalkBarChartEntries()

        observeVehicleLineChartEntries()
        observeRunLineChartEntries()
        observeWalkLineChartEntries()

        observeUserActivities()
    }

    protected abstract fun setUpCardView()

    private fun setUpBarChartSpinner(spinner: Spinner?) {
        val spinnerItems = resources.getStringArray(R.array.spinner_activities)

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

    protected abstract fun observeUserKilometersTravelled()

    protected abstract fun observeVehicleBarChartEntries()

    protected abstract fun observeRunTimeSpent()

    protected abstract fun observeUserRunDistanceDone()

    protected abstract fun observeRunBarChartEntries()

    protected abstract fun observeStillTimeSpent()

    protected abstract fun observeStillBarChartEntries()

    protected abstract fun observeWalkTimeSpent()

    protected abstract fun observeUserSteps()

    protected abstract fun observeWalkBarChartEntries()

    protected abstract fun observeVehicleLineChartEntries()

    protected abstract fun observeRunLineChartEntries()

    protected abstract fun observeWalkLineChartEntries()

    protected abstract fun observeUserActivities()

    private fun showVehicleBarChart(range: Pair<String, String>) {
        runBarChart.visibility = View.GONE
        walkBarChart.visibility = View.GONE
        stillBarChart.visibility = View.GONE

        vehicleBarChart.visibility = View.VISIBLE

        infoSecondColumn.visibility = View.VISIBLE
        titleSecondColumn.visibility = View.VISIBLE

        loadVehicleTime(range.first, range.second)
        loadKilometersTravelled(range.first, range.second)
        defineVehicleBarChartEntries(range.first, range.second)
    }

    protected abstract fun loadVehicleTime(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun loadKilometersTravelled(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun defineVehicleBarChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showRunBarChart(range: Pair<String, String>) {
        walkBarChart.visibility = View.GONE
        stillBarChart.visibility = View.GONE
        vehicleBarChart.visibility = View.GONE

        runBarChart.visibility = View.VISIBLE

        infoSecondColumn.visibility = View.VISIBLE
        titleSecondColumn.visibility = View.VISIBLE

        loadRunTime(range.first, range.second)
        loadRunDistanceDone(range.first, range.second)
        defineRunBarChartEntries(range.first, range.second)
    }

    protected abstract fun loadRunTime(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun loadRunDistanceDone(startOfPeriod: String, endOfPeriod: String)

    protected abstract fun defineRunBarChartEntries(startOfWeek: String, endOfWeek: String)

    private fun showStillBarChart(range: Pair<String, String>) {
        runBarChart.visibility = View.GONE
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
        runBarChart.visibility = View.GONE
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

    private fun showRunLineChart(range: Pair<String, String>) {
        walkLineChart.visibility = View.GONE
        vehicleLineChart.visibility = View.GONE

        runLineChart.visibility = View.VISIBLE

        defineRunLineChartEntries(range.first, range.second)
    }

    protected abstract fun defineRunLineChartEntries(startOfWeek: String, endOfWeek: String)

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