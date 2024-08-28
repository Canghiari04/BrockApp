package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB

import android.os.Bundle
import android.view.View
import java.time.YearMonth
import java.time.LocalDate
import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.launch
import android.widget.ImageButton
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

class ChartsFragment: Fragment(R.layout.charts_fragment) {
    val formatter = DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT)

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

        for (day in 1..currentDate.lengthOfMonth()) {
            val date = currentDate.withDayOfMonth(day)

            val (startOfDay, endOfDay) = getDayRange(date)
            val listWalkingActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(User.id, startOfDay, endOfDay)

            val totalSteps = listWalkingActivities.map { it.stepNumber }.sum()
            entries.add(BarEntry(day.toFloat(), totalSteps.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Numero di passi")
        val color = Color.parseColor("#BB2222")
        dataSet.color = color

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

        for (day in 1..currentDate.lengthOfMonth()) {
            val date = currentDate.withDayOfMonth(day)
            val (startOfDay, endOfDay) = getDayRange(date)

            val totalDistanceTravelled = db.UserVehicleActivityDao().getEndingVehicleActivitiesByUserIdAndPeriod(User.id, startOfDay, endOfDay).parallelStream().mapToDouble { it.distanceTravelled!! }.sum()
            entries.add(BarEntry(day.toFloat(), totalDistanceTravelled.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Distanza percorsa su veicoli")
        val color = Color.parseColor("#BB2222")
        dataSet.color = color

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

        val userWalkActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(User.id, startOfMonth, endOfMonth)
        val userWalkActivityCount = userWalkActivities.size

        val userStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(User.id, startOfMonth, endOfMonth)
        val userStillActivityCount = userStillActivities.size

        val userVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(User.id, startOfMonth, endOfMonth)
        val userVehicleActivityCount = userVehicleActivities.size

        withContext(Dispatchers.Main) {
            val noActivityMessage = view?.findViewById<TextView>(R.id.no_activity_message)

            if (userWalkActivityCount > 0 || userStillActivityCount > 0 || userVehicleActivityCount > 0) {
                entries.add(PieEntry(userVehicleActivityCount.toFloat(), "Vehicle activity"))
                entries.add(PieEntry(userStillActivityCount.toFloat(), "Still activity"))
                entries.add(PieEntry(userWalkActivityCount.toFloat(), "Walk activity"))

                val dataSet = PieDataSet(entries, "Dati")
                dataSet.colors = ColorTemplate.PASTEL_COLORS.toList()

                val data = PieData(dataSet)
                data.setDrawValues(false)
                activityTypePieChart.data = data
                activityTypePieChart.description?.isEnabled = false

                activityTypePieChart.setUsePercentValues(true)
                activityTypePieChart.setDrawEntryLabels(false)
                activityTypePieChart.invalidate()

                noActivityMessage?.visibility = View.GONE
                activityTypePieChart.visibility = View.VISIBLE
            } else {
                noActivityMessage?.visibility = View.VISIBLE
                activityTypePieChart.visibility = View.GONE
            }
        }
    }

    private fun getDayRange(date: LocalDate): Pair<String, String> {
        val startOfMonth = date.atTime(0, 0, 0)
        val endOfMonth = date.atTime(23, 59, 59)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(
            startOfMonth.format(outputFormatter),
            endOfMonth.format(outputFormatter)
        )
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