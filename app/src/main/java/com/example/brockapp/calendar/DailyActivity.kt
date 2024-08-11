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
        val textView = findViewById<TextView>(R.id.date_text_view)
        textView.text = getPrettyDate(date)

        lifecycleScope.launch(Dispatchers.Main) {
            val sortedActivities = getUserActivities(date)

            populateDailyActivitiesRecyclerView(sortedActivities, dailyList)
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
            val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, STILL_ACTIVITY_TYPE)
            listActivities.add(newActivity)
        }

        listVehicleActivities.parallelStream().forEach {
            val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, VEHICLE_ACTIVITY_TYPE)
            listActivities.add(newActivity)
        }

        listWalkingActivities.parallelStream().forEach {
            val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, WALK_ACTIVITY_TYPE)
            listActivities.add(newActivity)
        }

        return listActivities.sortedBy { it.timestamp }
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

    /**
     * Metodo definito per popolare la recycler view tramite la lista delle attività dell'utente.
     */
    private fun populateDailyActivitiesRecyclerView(activities: List<UserActivity>, dailyList: RecyclerView) {

        //mostro a video le attività con transition type = 1
        val adapterActivities = DailyActivityAdapter(activities.filter { it.transitionType == 1 }, {activityId, type -> onItemClick(activityId, type)})
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager

        var entries = mutableListOf<PieEntry>()
        val pieChart = findViewById<PieChart>(R.id.daily_activity_pie_chart)

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



    private fun onItemClick(activityId: Long, type: String) {

        // PRENDERE IL DIALOG
        // PASSARGLI I DATI
        // DIALOG INTERNAMENTE QUERY AL DB ROOM
        // ASSOCIAZIONE DEI DATI ESTRAPOLATI NELLA VIEW
    }

    private fun computeTimeSpent(
        userWalkActivities: List<UserActivity>
    ): Long {
        var timeSpentWalking = 0L
        val dateFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        for (i in userWalkActivities.indices) {
            if(userWalkActivities[i].transitionType == 1)
                continue

            val beginActivityTime = LocalDateTime.parse(userWalkActivities[i].timestamp, dateFormatter)
            val nextActivity =
                if (i < userWalkActivities.size - 1) userWalkActivities[i + 1] else null

            if (nextActivity == null) break

            val endActivityTime = LocalDateTime.parse(nextActivity.timestamp, dateFormatter)

            // Calcola la durata in millisecondi tra due LocalDateTime
            val duration = Duration.between(beginActivityTime, endActivityTime)
            val durationInMinutes = duration.toMinutes()

            timeSpentWalking += durationInMinutes

        }

        return timeSpentWalking
    }

}
