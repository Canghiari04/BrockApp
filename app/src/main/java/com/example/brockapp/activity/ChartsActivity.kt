package com.example.brockapp.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.CHARTS_DATE_FORMAT
import com.example.brockapp.ISO_DATE_FORMAT
import com.example.brockapp.R
import com.example.brockapp.activity.CalendarActivity.Companion.user
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserWalkActivityEntity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class ChartsActivity : AppCompatActivity() {

    private val db = BrockDB.getInstance(this)
    private lateinit var stepCountBarChart: BarChart
    private lateinit var distanceTravelledBarChart: BarChart
    private lateinit var activityTypePieChart: PieChart

    private val formatter = DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.charts_activity)
        setDate(YearMonth.now())

        // Inizializza i grafici
        stepCountBarChart = findViewById(R.id.step_count_bar_chart)
        distanceTravelledBarChart = findViewById(R.id.distance_travelled_bar_chart)
        activityTypePieChart = findViewById(R.id.activity_type_pie_chart)

        val buttonBack = findViewById<ImageButton>(R.id.charts_button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.charts_button_forward_month)

        setButtonOnClickListener(buttonBack, buttonForward)

        setupCharts()
    }

    private fun setButtonOnClickListener(
        buttonBack: ImageButton,
        buttonForward: ImageButton
    ) {
        buttonBack.setOnClickListener {
            val date = getDateFromTextView().minusMonths(1)
            setDate(date)
            setupCharts()
        }

        buttonForward.setOnClickListener {
            val date = getDateFromTextView().plusMonths(1)
            setDate(date)
            setupCharts()
        }
    }

    private fun getDateFromTextView(): YearMonth {
        val strDate = findViewById<TextView>(R.id.charts_date_text_view).text
        return YearMonth.parse(strDate, formatter)
    }

    private fun setDate(date: YearMonth) {
        findViewById<TextView>(R.id.charts_date_text_view).text = date.format(formatter)
    }

    private fun setupCharts() {
        CoroutineScope(Dispatchers.IO).launch {
            setupStepCountBarChart()
            setupDistanceTravelledBarChart()
            setupActivityTypePieChart()
        }
    }

    private suspend fun setupStepCountBarChart() {
        val entries = mutableListOf<BarEntry>()
        val yearMonth = getDateFromTextView()
        val currentDate = yearMonth.atDay(1)

        for (day in 1..currentDate.lengthOfMonth()) {
            val date = currentDate.withDayOfMonth(day)
            val (startOfDay, endOfDay) = getDayRange(date)
            val totalSteps = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
                .sumOf { it.stepNumber }

            entries.add(BarEntry(day.toFloat(), totalSteps.toFloat()))
        }

        val dataSet = getDataSet(entries, "Numero di passi")

        val data = BarData(dataSet).apply {
            barWidth = 0.9f // Adjust bar width
        }

        //Usa context main poichè solo il main thread può modificare oggetti nella view
        withContext(Dispatchers.Main) {
            stepCountBarChart.apply {
                this.data = data
                xAxis.valueFormatter = IndexAxisValueFormatter((1..currentDate.lengthOfMonth()).map { it.toString() })
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                stepCountBarChart.getAxisLeft().setDrawGridLines(false)
                stepCountBarChart.getXAxis().setDrawGridLines(false)
                stepCountBarChart.legend.isEnabled = false
                stepCountBarChart.description.isEnabled = false
                invalidate()
            }
        }
    }



    private suspend fun setupDistanceTravelledBarChart() {
        val entries = mutableListOf<BarEntry>()
        val yearMonth = getDateFromTextView()
        val currentDate = yearMonth.atDay(1)

        for (day in 1..currentDate.lengthOfMonth()) {
            val date = currentDate.withDayOfMonth(day)
            val (startOfDay, endOfDay) = getDayRange(date)
            val totalDistanceTravelled = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
                .sumOf { it.distanceTravelled ?: 0.0 }

            entries.add(BarEntry(day.toFloat(), totalDistanceTravelled.toFloat()))
        }

        val dataSet = getDataSet(entries, "Distanza percorsa")

        val data = BarData(dataSet).apply{
            barWidth = 0.9f // Adjust bar width
        }

        withContext(Dispatchers.Main) {
            distanceTravelledBarChart.apply {
                this.data = data
                xAxis.valueFormatter = IndexAxisValueFormatter((1..currentDate.lengthOfMonth()).map { it.toString() })
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                distanceTravelledBarChart.getAxisLeft().setDrawGridLines(false)
                distanceTravelledBarChart.getXAxis().setDrawGridLines(false)
                distanceTravelledBarChart.legend.isEnabled = false
                distanceTravelledBarChart.description.isEnabled = false
                invalidate()
            }
        }
    }

    private fun getDataSet(entries: MutableList<BarEntry>, label: String): BarDataSet {

        return BarDataSet(entries, label).apply {

            colors = entries.map {
                val valueToColor = when {
                    it.y > 10000 -> 1f
                    else -> {
                        it.y / 10
                    }
                }
                val green = valueToColor
                val blue = 1 - green

                // Crea il colore ARGB
                android.graphics.Color.argb(255,0, (green * 255).toInt(), (blue * 255).toInt())
            }
        }
    }


    private suspend fun setupActivityTypePieChart() {
        val entries = mutableListOf<PieEntry>()
        val yearMonth = getDateFromTextView()
        val (startOfMonth, endOfMonth) = getMonthRange(yearMonth.atDay(1))

        val userWalkActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
        val userStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
        val userVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)

        entries.apply {
            add(PieEntry(userVehicleActivities.size.toFloat(), "Vehicle activity"))
            add(PieEntry(userStillActivities.size.toFloat(), "Still activity"))
            add(PieEntry(userWalkActivities.size.toFloat(), "Walk activity"))
        }

        val dataSet = PieDataSet(entries, "Dati").apply {
            colors = ColorTemplate.PASTEL_COLORS.toList()
        }

        val data = PieData(dataSet)

        withContext(Dispatchers.Main) {
            activityTypePieChart.apply {
                this.data = data
                invalidate()
            }

            val noActivityMessage = findViewById<TextView>(R.id.no_activity_message)
            if (entries.isNotEmpty()) {
                noActivityMessage.visibility = View.GONE
                activityTypePieChart.visibility = View.VISIBLE
            } else {
                noActivityMessage.visibility = View.VISIBLE
                activityTypePieChart.visibility = View.GONE
            }
        }
    }

    private fun getDayRange(date: LocalDate): Pair<String, String> {
        val startOfDay = date.atTime(0, 0, 0)
        val endOfDay = date.atTime(23, 59, 59)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)
        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }

    private fun getMonthRange(date: LocalDate): Pair<String, String> {
        val startOfMonth = date.withDayOfMonth(1).atStartOfDay()
        val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)
        return Pair(startOfMonth.format(outputFormatter), endOfMonth.format(outputFormatter))
    }
}
