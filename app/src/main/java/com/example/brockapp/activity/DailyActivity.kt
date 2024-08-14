package com.example.brockapp.activity

import android.content.Intent
import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.DATE_SEPARATOR
import com.example.brockapp.ISO_DATE_FORMAT
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.viewmodel.DailyViewModel
import com.example.brockapp.calendar.DailyActivityAdapter
import com.example.brockapp.viewmodel.DailyViewModelFactory

import android.os.Bundle
import java.time.Duration
import java.time.LocalDate
import android.view.MenuItem
import android.widget.TextView
import java.time.LocalDateTime
import androidx.appcompat.widget.Toolbar
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.PieData
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import androidx.recyclerview.widget.LinearLayoutManager

class DailyActivity: AppCompatActivity() {
    private lateinit var user: User
    private lateinit var viewModelDaily: DailyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val date: String? = intent.getStringExtra("ACTIVITY_DATE")

        val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
        val pieChart = findViewById<PieChart>(R.id.daily_activity_pie_chart)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_daily_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val textView = findViewById<TextView>(R.id.date_text_view)
        textView.text = getPrettyDate(date)

        val db = BrockDB.getInstance(this)
        val factoryViewModelDaily = DailyViewModelFactory(db)

        viewModelDaily = ViewModelProvider(this, factoryViewModelDaily)[DailyViewModel::class.java]
        user = User.getInstance()

        viewModelDaily.getUserActivities(date, user)

        viewModelDaily.sortedList.observe(this) { sortedActivities ->
            if(sortedActivities.isNotEmpty()) {
                populateDailyActivitiesChart(pieChart, sortedActivities)
                populateDailyActivitiesRecyclerView(dailyList, sortedActivities)
            } else {
                // TODO --> GESTIRE IL FATTO CHE NON CI SIANO ATTIVITÀ
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "calendar"))
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    private fun getPrettyDate(strDate: String?): String {
        val tokens = strDate!!.split(DATE_SEPARATOR)
        val date = LocalDate.of(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        return "${date.dayOfWeek}, ${tokens[2]} ${date.month}".lowercase()
    }

    private fun populateDailyActivitiesChart(pieChart: PieChart, activities: List<UserActivity>) {
        val entries = mutableListOf<PieEntry>()

        val userWalkActivities = activities.filter { it.type == WALK_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentWalking = computeTimeSpent(userWalkActivities)

        val userStillActivities = activities.filter { it.type == STILL_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentStill = computeTimeSpent(userStillActivities)

        val userVehicleActivities = activities.filter { it.type == VEHICLE_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentVehicle = computeTimeSpent(userVehicleActivities)

        entries.apply {
            add(PieEntry(timeSpentWalking.toFloat(), "Tempo in cammino"))
            add(PieEntry(timeSpentStill.toFloat(), "Tempo fermo"))
            add(PieEntry(timeSpentVehicle.toFloat(), "Tempo in veicolo"))
        }

        val dataSet = PieDataSet(entries, "Dati").apply {
            colors = ColorTemplate.PASTEL_COLORS.toList()
        }

        val data = PieData(dataSet)

        pieChart.apply {
            this.data = data
            setDrawEntryLabels(false)
            invalidate()
        }
    }

    /**
     * Metodo definito per popolare la RecyclerView tramite la lista delle attività dell'utente.
     */
    private fun populateDailyActivitiesRecyclerView(dailyList: RecyclerView, activities: List<UserActivity>) {
        val adapterActivities = DailyActivityAdapter(activities.filter { it.transitionType == 1 })
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }

    private fun computeTimeSpent(userActivities: List<UserActivity>): Long {
        var timeSpentWalking = 0L
        val dateFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        for(i in userActivities.indices) {
            if(userActivities[i].transitionType == 1)
                continue

            val beginActivityTime = LocalDateTime.parse(userActivities[i].timestamp, dateFormatter)
            val nextActivity = if (i < userActivities.size - 1) userActivities[i + 1] else null

            if(nextActivity == null)
                break

            val endActivityTime = LocalDateTime.parse(nextActivity.timestamp, dateFormatter)

            val duration = Duration.between(beginActivityTime, endActivityTime)
            val durationInMinutes = duration.toMinutes()

            timeSpentWalking += durationInMinutes
        }

        return timeSpentWalking
    }
}