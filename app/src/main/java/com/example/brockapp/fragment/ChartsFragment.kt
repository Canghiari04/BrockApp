package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB

import android.os.Bundle
import android.view.View
import java.time.YearMonth
import java.time.LocalTime
import java.time.LocalDate
import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.launch
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.lifecycleScope
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import androidx.core.widget.addTextChangedListener
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChartsFragment: Fragment(R.layout.fragment_charts) {
    private val formatter = DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT)

    private lateinit var db : BrockDB
    private lateinit var dateTextView: TextView
    private lateinit var stepCountBarChart: BarChart
    private lateinit var activityTypePieChart: PieChart
    private lateinit var distanceTravelledBarChart: BarChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepCountBarChart = view.findViewById(R.id.step_count_bar_chart)
        distanceTravelledBarChart = view.findViewById(R.id.distance_travelled_bar_chart)
        activityTypePieChart = view.findViewById(R.id.activity_type_pie_chart)
        dateTextView = view.findViewById(R.id.charts_date_text_view)

        val buttonBack = view.findViewById<ImageButton>(R.id.charts_button_back_month)
        val buttonForward = view.findViewById<ImageButton>(R.id.charts_button_forward_month)

        setDate(YearMonth.now())
        setButtonOnClickListener(buttonBack, buttonForward)

        db = BrockDB.getInstance(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            setupCharts()
        }
    }

    private fun setButtonOnClickListener(buttonBack: ImageButton, buttonForward: ImageButton) {
        buttonBack.setOnClickListener {
            val strDate = view?.findViewById<TextView>(R.id.charts_date_text_view)?.text

            var date = YearMonth.parse(strDate, formatter)
            date = date.minusMonths(1)
            setDate(date)
        }

        buttonForward.setOnClickListener {
            val strDate = view?.findViewById<TextView>(R.id.charts_date_text_view)?.text

            var date = YearMonth.parse(strDate, formatter)
            date = date.plusMonths(1)
            setDate(date)
        }

        dateTextView.addTextChangedListener(
            afterTextChanged = {
                CoroutineScope(Dispatchers.IO).launch {
                    setupCharts()
                }
            }
        )
    }

    private fun setDate(date: YearMonth) {
        dateTextView.text = date.format(DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT)).toString()
    }

    private suspend fun setupCharts() {
        lifecycleScope.launch (Dispatchers.Main){
            setupStepCountBarChart()
            setupDistanceTravelledBarChart()
            setupActivityTypePieChart()
        }
    }

    private suspend fun setupStepCountBarChart() {
        val entries = ArrayList<BarEntry>()
        val dateStr: String = dateTextView.text.toString()

        val yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))
        val currentDate = yearMonth.atDay(1)
        val startOfMonth = currentDate.atStartOfDay()
        val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).atTime(LocalTime.MAX)

        val listWalkingActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(User.id, startOfMonth.toString(), endOfMonth.toString())

        val stepsPerDay = listWalkingActivities.groupBy {
            it.timestamp?.let { timestamp ->
                LocalDate.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME).dayOfMonth
            } ?: 0
        }.mapValues { entry ->
            entry.value.sumOf { it.stepNumber }
        }

        for (day in 1..currentDate.lengthOfMonth()) {
            val totalSteps = stepsPerDay[day] ?: 0f
            entries.add(BarEntry(day.toFloat(), totalSteps.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Numero di passi")
        dataSet.color = Color.parseColor("#BB2222")

        val data = BarData(dataSet)
        stepCountBarChart.data = data

        stepCountBarChart.xAxis.valueFormatter = IndexAxisValueFormatter((1..currentDate.lengthOfMonth()).map { it.toString() })
        stepCountBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        stepCountBarChart.xAxis.setDrawGridLines(false)
        stepCountBarChart.axisLeft.axisMinimum = 0f
        stepCountBarChart.axisRight.axisMinimum = 0f
        stepCountBarChart.animateY(500)
        stepCountBarChart.description.isEnabled = false
        stepCountBarChart.legend.isEnabled = false
        stepCountBarChart.invalidate()
    }


    private suspend fun setupDistanceTravelledBarChart() {
        val entries = ArrayList<BarEntry>()
        val dateStr: String = dateTextView.text.toString()

        val yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))

        val currentDate = yearMonth.atDay(1)
        val startOfMonth = currentDate.atStartOfDay()
        val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).atTime(LocalTime.MAX)

        val listVehicleActivities = db.UserVehicleActivityDao().getEndingVehicleActivitiesByUserIdAndPeriod(User.id, startOfMonth.toString(), endOfMonth.toString())

        val distancePerDay = listVehicleActivities.groupBy {
            it.timestamp?.let { timestamp ->
                LocalDate.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME).dayOfMonth
            } ?: 0
        }.mapValues { entry ->
            entry.value.sumOf { it.distanceTravelled?: 0.0 }
        }

        for (day in 1..currentDate.lengthOfMonth()) {
            val totalDistanceTravelled = distancePerDay[day] ?: 0.0
            entries.add(BarEntry(day.toFloat(), totalDistanceTravelled.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Distanza percorsa su veicoli")
        dataSet.color = Color.parseColor("#BB2222")

        val data = BarData(dataSet)
        data.setDrawValues(false)
        distanceTravelledBarChart.data = data

        distanceTravelledBarChart.xAxis.valueFormatter = IndexAxisValueFormatter((1..currentDate.lengthOfMonth()).map { it.toString() })
        distanceTravelledBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        distanceTravelledBarChart.axisLeft.axisMinimum = 0f
        distanceTravelledBarChart.axisRight.axisMinimum = 0f
        distanceTravelledBarChart.animateY(500)
        distanceTravelledBarChart.xAxis.setDrawGridLines(false)
        distanceTravelledBarChart.description.isEnabled = false
        distanceTravelledBarChart.legend.isEnabled = false
        distanceTravelledBarChart.invalidate()
    }


    private suspend fun setupActivityTypePieChart() {
        val entries = ArrayList<PieEntry>()

        val dateStr = dateTextView.text
        val yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))
        val currentDate = yearMonth.atDay(1)

        val (startOfMonth, endOfMonth) = getMonthRange(currentDate)

        val userWalkActivitiesCount = db.UserWalkActivityDao().getWalkActivitiesCountByUserIdAndPeriod(User.id, startOfMonth, endOfMonth)
        val userStillActivitiesCount = db.UserStillActivityDao().getStillActivitiesCountByUserIdAndPeriod(User.id, startOfMonth, endOfMonth)
        val userVehicleActivitiesCount = db.UserVehicleActivityDao().getVehicleActivitiesCountByUserIdAndPeriod(User.id, startOfMonth, endOfMonth)

        withContext(Dispatchers.Main) {
            if (userWalkActivitiesCount > 0 || userStillActivitiesCount > 0 || userVehicleActivitiesCount > 0) {
                entries.add(PieEntry(userWalkActivitiesCount.toFloat(), "Walk activity"))
                entries.add(PieEntry(userVehicleActivitiesCount.toFloat(), "Vehicle activity"))
                entries.add(PieEntry(userStillActivitiesCount.toFloat(), "Still activity"))

                val dataSet = PieDataSet(entries, "Dati")
                dataSet.colors = ColorTemplate.PASTEL_COLORS.toList()

                val data = PieData(dataSet)
                data.setDrawValues(false)
                activityTypePieChart.data = data
                activityTypePieChart.description?.isEnabled = false

                activityTypePieChart.setUsePercentValues(true)
                activityTypePieChart.setDrawEntryLabels(false)
                activityTypePieChart.invalidate()

                activityTypePieChart.visibility = View.VISIBLE
            } else {
                activityTypePieChart.visibility = View.GONE
                Toast.makeText(requireContext(), "Nessuna attività compiuta nel mese.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMonthRange(date: LocalDate): Pair<String, String> {
        val startOfMonth = date.withDayOfMonth(1).atStartOfDay()
        val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(
            startOfMonth.format(outputFormatter),
            endOfMonth.format(outputFormatter)
        )
    }
}