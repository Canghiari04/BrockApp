package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.CalendarUtil
import com.example.brockapp.data.UserActivity
import com.example.brockapp.adapter.DailyActivityAdapter
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.interfaces.TimeSpentCounterImpl
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

import android.os.Bundle
import java.time.LocalDate
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import androidx.recyclerview.widget.LinearLayoutManager

class DailyActivity: AppCompatActivity() {
    private var utilCalendar: CalendarUtil = CalendarUtil()
    private val timeSpentCounter = TimeSpentCounterImpl()

    private lateinit var viewModel: ActivitiesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_activity)

        val user = User.getInstance()
        val db = BrockDB.getInstance(this)
        val date: String? = intent.getStringExtra("ACTIVITY_DATE")

        val textView = findViewById<TextView>(R.id.date_text_view)
        textView.text = utilCalendar.getPrettyDate(date)

        val factoryViewModelDaily = ActivitiesViewModelFactory(db)
        viewModel = ViewModelProvider(this, factoryViewModelDaily)[ActivitiesViewModel::class.java]

        observeDailyActivities()

        val (startOfDay, endOfDay) = getDayRange(date)
        viewModel.getUserActivities(startOfDay, endOfDay, user)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Calendario")
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    private fun observeDailyActivities() {
        viewModel.listTimeStampActivities.observe(this) { listTimeStampActivities ->
            if (listTimeStampActivities.isNotEmpty()) {
                utilCalendar = CalendarUtil()

                val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
                populateDailyActivitiesRecyclerView(dailyList, listTimeStampActivities)

                val pieChart = findViewById<PieChart>(R.id.daily_activity_pie_chart)
                populateDailyActivitiesChart(pieChart, listTimeStampActivities)
            } else {
                setContentView(R.layout.activity_empty_page)
            }

            setUpToolBar()
        }
    }

    private fun getDayRange(dateStr: String?): Pair<String, String> {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val startOfDay = date.atStartOfDay().withSecond(0)
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }

    private fun populateDailyActivitiesRecyclerView(dailyList: RecyclerView, activities: List<UserActivity>) {
        val adapter = DailyActivityAdapter(activities)
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapter
        dailyList.layoutManager = layoutManager
    }

    private fun populateDailyActivitiesChart(pieChart: PieChart, activities: List<UserActivity>) {
        val entries = mutableListOf<PieEntry>()

        val userWalkActivities = activities.filter { it.type == WALK_ACTIVITY_TYPE }
        val timeSpentWalking = timeSpentCounter.computeTimeSpent(userWalkActivities)

        val userStillActivities = activities.filter { it.type == STILL_ACTIVITY_TYPE }
        val timeSpentStill = timeSpentCounter.computeTimeSpent(userStillActivities)

        val userVehicleActivities = activities.filter { it.type == VEHICLE_ACTIVITY_TYPE }
        val timeSpentVehicle = timeSpentCounter.computeTimeSpent(userVehicleActivities)

        val secondsInDay = 60 * 60 * 24
        val totalRecordedTime = timeSpentWalking + timeSpentStill + timeSpentVehicle

        val unknownTime = (secondsInDay - totalRecordedTime).toFloat()

        entries.apply {
            add(PieEntry(timeSpentStill.toFloat(), "Attività sedentaria"))
            add(PieEntry(timeSpentVehicle.toFloat(), "Viaggio in macchina"))
            add(PieEntry(timeSpentWalking.toFloat(), "Camminata"))
            add(PieEntry(unknownTime, "Unknown"))
        }

        val dataSet = PieDataSet(entries, " ").apply {
            colors = ColorTemplate.PASTEL_COLORS.toList()
        }

        val data = PieData(dataSet)

        pieChart.apply {
            invalidate()
            this.data = data
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            description.isEnabled = false
        }
    }

    /**
     * Metodo attuato per personalizzare il comportamento dell'action bar dell'activity.
     */
    private fun setUpToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_daily_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}