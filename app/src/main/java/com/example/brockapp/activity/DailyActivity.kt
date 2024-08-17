package com.example.brockapp.activity

import android.content.Intent
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.util.CalendarUtil
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.viewmodel.DailyViewModel
import com.example.brockapp.calendar.DailyActivityAdapter
import com.example.brockapp.viewmodel.DailyViewModelFactory

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
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
    private lateinit var utilCalendar: CalendarUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val date: String? = intent.getStringExtra("ACTIVITY_DATE")

        val db = BrockDB.getInstance(this)
        val factoryViewModelDaily = DailyViewModelFactory(db)

        viewModelDaily = ViewModelProvider(this, factoryViewModelDaily)[DailyViewModel::class.java]
        user = User.getInstance()

        viewModelDaily.getUserActivities(date, user)

        viewModelDaily.sortedList.observe(this) { item ->
            if(item.isNotEmpty()) {
                setContentView(R.layout.daily_activity_activity)

                utilCalendar = CalendarUtil()

                val textView = findViewById<TextView>(R.id.date_text_view)
                textView.text = utilCalendar.getPrettyDate(date)

                val pieChart = findViewById<PieChart>(R.id.daily_activity_pie_chart)
                val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)

                populateDailyActivitiesChart(pieChart, item)
                populateDailyActivitiesRecyclerView(dailyList, item)
            } else {
                setContentView(R.layout.daily_empty_activity)
            }

            setUpActionBar()
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

    private fun populateDailyActivitiesChart(pieChart: PieChart, activities: List<UserActivity>) {
        val entries = mutableListOf<PieEntry>()

        val userWalkActivities = activities.filter { it.type == WALK_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentWalking = utilCalendar.computeTimeSpent(userWalkActivities)

        val userStillActivities = activities.filter { it.type == STILL_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentStill = utilCalendar.computeTimeSpent(userStillActivities)

        val userVehicleActivities = activities.filter { it.type == VEHICLE_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentVehicle = utilCalendar.computeTimeSpent(userVehicleActivities)

        entries.apply {
            add(PieEntry(timeSpentWalking.toFloat(), "CAMMINO"))
            add(PieEntry(timeSpentStill.toFloat(), "STAZIONARIO"))
            add(PieEntry(timeSpentVehicle.toFloat(), "VEICOLO"))
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

    private fun populateDailyActivitiesRecyclerView(dailyList: RecyclerView, activities: List<UserActivity>) {
        val adapterActivities = DailyActivityAdapter(activities.filter { it.transitionType == 1 })
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }

    /**
     * Metodo attuato per personalizzare il comportamento dell'action bar dell'activity.
     */
    private fun setUpActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_daily_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}