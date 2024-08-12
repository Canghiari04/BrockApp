package com.example.brockapp.calendar

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.ISO_DATE_FORMAT
import com.example.brockapp.R
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.User
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.data.UserActivity
import com.example.brockapp.database.BrockDB
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DailyActivity : AppCompatActivity() {
    private val user = User.getInstance()
    private val db = BrockDB.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val date: String? = intent.getStringExtra("ACTIVITY_DATE")

        val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
        val pieChart = findViewById<PieChart>(R.id.daily_activity_pie_chart)
        val textView = findViewById<TextView>(R.id.date_text_view)
        textView.text = getPrettyDate(date)

        lifecycleScope.launch(Dispatchers.Main) {
            val sortedActivities = getUserActivities(date)
            populateDailyActivitiesChart(pieChart, sortedActivities)
            populateDailyActivitiesRecyclerView(dailyList, sortedActivities)
        }
    }

    private fun getPrettyDate(strDate: String?): String {
        val tokens = strDate!!.split("-")
        val date = LocalDate.of(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        return "${date.dayOfWeek}, ${tokens[2]} ${date.month}".lowercase()
    }

    /**
     * In base al range temporale, è restituita la lista delle attività effettuate dall'utente in
     * ordine temporale.
     */
    private suspend fun getUserActivities(date: String?): List<UserActivity> {
        val (startOfDay, endOfDay) = getDayRange(date)
        val listActivities = ArrayList<UserActivity>()

        val listStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
        val listVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
        val listWalkingActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)

        listStillActivities.parallelStream().forEach {
            val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, STILL_ACTIVITY_TYPE, "METTERE DURATA STILL")
            listActivities.add(newActivity)
        }

        listVehicleActivities.parallelStream().forEach {
            val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, VEHICLE_ACTIVITY_TYPE, it.distanceTravelled.toString())
            listActivities.add(newActivity)
        }

        listWalkingActivities.parallelStream().forEach {
            val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, WALK_ACTIVITY_TYPE, it.stepNumber.toString())
            listActivities.add(newActivity)
        }

        return listActivities.sortedBy { it.timestamp }
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
     * Metodo definito per popolare la recycler view tramite la lista delle attività dell'utente.
     */
    private fun populateDailyActivitiesRecyclerView(dailyList: RecyclerView, activities: List<UserActivity>) {
        val adapterActivities = DailyActivityAdapter(activities.filter { it.transitionType == 1 })
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }

    /**
     * Funzione attuata per definire il range temporale in cui ricercare le attività effettuate
     * dall'utente.
     */
    private fun getDayRange(dateStr: String?): Pair<String, String> {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val startOfDay = date.atStartOfDay().withSecond(0)
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
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