package com.example.brockapp.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.CALENDAR_DATE_FORMAT
import com.example.brockapp.CHARTS_DATE_FORMAT
import com.example.brockapp.DATE_FORMAT
import com.example.brockapp.R
import com.example.brockapp.activity.CalendarActivity.Companion.user
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserWalkActivityEntity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


class ChartsActivity : AppCompatActivity() {


    private val db = BrockDB.getInstance(this)
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    val formatter = DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.charts_activity)
        setDate(YearMonth.now())

        // Inizializza i grafici
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)

        val buttonBack = findViewById<ImageButton>(R.id.charts_button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.charts_button_forward_month)

        setButtonOnClickListener(buttonBack, buttonForward)

        CoroutineScope(Dispatchers.IO).launch {
            setupBarChart()
            setupPieChart()
        }

    }

    private fun setButtonOnClickListener(
        buttonBack: ImageButton,
        buttonForward: ImageButton
    ) {
        buttonBack.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.charts_date_text_view).text

            var date = YearMonth.parse(strDate, formatter)

            date = date.minusMonths(1)

            setDate(date)

            CoroutineScope(Dispatchers.IO).launch {
                setupBarChart()
                setupPieChart()
            }
        }

        buttonForward.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.charts_date_text_view).text


            var date = YearMonth.parse(strDate, formatter)

            date = date.plusMonths(1)

            setDate(date)

            CoroutineScope(Dispatchers.IO).launch {
                setupBarChart()
                setupPieChart()
            }
        }
    }

    private fun setDate(date: YearMonth) {
        findViewById<TextView>(R.id.charts_date_text_view).text = date.format(DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT)).toString()
    }

    private suspend fun setupBarChart() {

        val entries = ArrayList<BarEntry>()
        val dateStr : String = findViewById<TextView>(R.id.charts_date_text_view).text.toString()

        val yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))

        val currentDate = yearMonth.atDay(1)

        for (day in 1..currentDate.lengthOfMonth()) {

            val date = currentDate.withDayOfMonth(day)

            val (startOfDay, endOfDay) = getDayRange(date)

            val listWalkingActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)

            // Sommiamo il numero di passi per il giorno corrente
            val totalSteps = listWalkingActivities.map { it.stepNumber }.sum()

            entries.add(BarEntry(day.toFloat(), totalSteps.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Numero di passi")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val data = BarData(dataSet)
        barChart.data = data

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter((1..currentDate.lengthOfMonth()).map { it.toString() }) // Etichette asse x come giorni del mese
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.invalidate()
    }

    private suspend fun setupPieChart() {
        val entries = ArrayList<PieEntry>()

        val dateStr = findViewById<TextView>(R.id.charts_date_text_view).text

        val yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))

        val currentDate = yearMonth.atDay(1)

        val (startOfMonth, endOfMonth) = getMonthRange(currentDate)
        Log.d("start", startOfMonth)
        Log.d("end", endOfMonth)

        val userWalkActivities : List<UserWalkActivityEntity> = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
        val userWalkActivityCount = userWalkActivities.size

        val userStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
        val userStillActivityCount = userStillActivities.size

        val userVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
        val userVehicleActivityCount = userVehicleActivities.size

        entries.add(PieEntry(userVehicleActivityCount.toFloat(), "Vehicle activity"))
        entries.add(PieEntry(userStillActivityCount.toFloat(), "Still activity"))
        entries.add(PieEntry(userWalkActivityCount.toFloat(), "Walk activity"))

        val dataSet = PieDataSet(entries, "Dati")
        dataSet.colors = ColorTemplate.PASTEL_COLORS.toList()

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun getDayRange(date: LocalDate): Pair<String, String> {

        val startOfMonth = date.atTime(0, 0, 0)

        val endOfMonth = date.atTime(23, 59, 59)

        // Formatter per la data di output nel formato richiesto
        val outputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)

        // Restituiamo la coppia di date
        return Pair(
            startOfMonth.format(outputFormatter),
            endOfMonth.format(outputFormatter)
        )
    }

    private fun getMonthRange(date: LocalDate): Pair<String, String> {
        val startOfMonth = date.withDayOfMonth(1).atStartOfDay()

        val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
        val outputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)

        // Restituiamo la coppia di date
        return Pair(
            startOfMonth.format(outputFormatter),
            endOfMonth.format(outputFormatter)
        )
    }
}