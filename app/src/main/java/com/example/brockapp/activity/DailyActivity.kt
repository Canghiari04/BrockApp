package com.example.brockapp.activity

import com.example.brockapp.*
import android.content.Intent
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.util.CalendarUtil
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.adapter.DailyActivityAdapter
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.utils.ColorTemplate

class DailyActivity: AppCompatActivity() {
    private var utilCalendar: CalendarUtil = CalendarUtil()

    private lateinit var viewModel: ActivitiesViewModel
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val date: String? = intent.getStringExtra("ACTIVITY_DATE")

        val db = BrockDB.getInstance(this)
        val factoryViewModelDaily = ActivitiesViewModelFactory(db)

        user = User.getInstance()

        viewModel = ViewModelProvider(this, factoryViewModelDaily)[ActivitiesViewModel::class.java]
        viewModel.getDayUserActivities(date, user)

        viewModel.sortedDayActivitiesList.observe(this) { item ->
            if(item.isNotEmpty()) {
                utilCalendar = CalendarUtil()

                val textView = findViewById<TextView>(R.id.date_text_view)
                textView.text = utilCalendar.getPrettyDate(date)

                val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
                populateDailyActivitiesRecyclerView(dailyList, item)

                val pieChart = findViewById<PieChart>(R.id.daily_activity_pie_chart)
                populateDailyActivitiesChart(pieChart, item)
            } else {
                setContentView(R.layout.empty_page)
            }

            setUpToolBar()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Calendar")
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

    private fun populateDailyActivitiesChart(pieChart: PieChart, activities: List<UserActivity>) {
        val entries = mutableListOf<PieEntry>()

        val userWalkActivities = activities.filter { it.type == WALK_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentWalking = utilCalendar.computeTimeSpent(userWalkActivities)

        val userStillActivities = activities.filter { it.type == STILL_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentStill = utilCalendar.computeTimeSpent(userStillActivities)

        val userVehicleActivities = activities.filter { it.type == VEHICLE_ACTIVITY_TYPE }.sortedBy { it.timestamp }
        val timeSpentVehicle = utilCalendar.computeTimeSpent(userVehicleActivities)

        val secondsInDay = 60 * 60 * 24
        val totalRecordedTime = timeSpentWalking + timeSpentStill + timeSpentVehicle

        val unknownTime = (secondsInDay - totalRecordedTime).toFloat()

        entries.apply {
            add(PieEntry(timeSpentWalking.toFloat(), "CAMMINO"))
            add(PieEntry(timeSpentStill.toFloat(), "STAZIONARIO"))
            add(PieEntry(timeSpentVehicle.toFloat(), "VEICOLO"))
            add(PieEntry(unknownTime, "Sconosciuto"))
        }

        val dataSet = PieDataSet(entries, "Dati").apply {
            colors = ColorTemplate.PASTEL_COLORS.toList()
        }

        val data = PieData(dataSet)

        pieChart.apply {
            this.data = data
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            description.isEnabled = false
            invalidate()
        }
    }

    private fun populateDailyActivitiesRecyclerView(dailyList: RecyclerView, activities: List<UserActivity>) {
        val adapterActivities = DailyActivityAdapter(activities)
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
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