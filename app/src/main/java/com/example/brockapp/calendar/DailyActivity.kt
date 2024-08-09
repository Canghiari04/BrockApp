package com.example.brockapp.calendar

import UserActivity
import com.example.brockapp.R
import com.example.brockapp.database.DbHelper
import com.example.brockapp.activity.CalendarActivity

import android.os.Bundle
import java.time.LocalDate
import android.widget.TextView
import java.time.format.DateTimeFormatter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class DailyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val date : String? = intent.getStringExtra("ACTIVITY_DATE")

        val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
        val textView = findViewById<TextView>(R.id.date_text_view)
        textView.text = getPrettyDate(date)

        val sortedActivities = getUserActivities(date)

        populateDailyActivitiesRecyclerView(sortedActivities, dailyList)
    }

    private fun getPrettyDate(strDate: String?): String {
        val tokens = strDate!!.split("-")
        val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt(), tokens[0].toInt())

        val timestamps: List<String> = userActivityList.map { it.timestamp }
        val walkActivityCount = listActivityWalk.size
        val vehicleActivityCount = listActivityVehicle.size
        val stillActivityCount = listActivityStill.size

    /**
     * In base al range temporale, è restituita la lista delle attività effettuate dall'utente in
     * ordine temporale.
     */
    private fun getUserActivities(date: String?): List<UserActivity> {
        val (startOfDay, endOfDay) = getDayRange(date)

        val listWalkingActivities = dbHelper.getUserWalkActivities(user.id, startOfDay, endOfDay)
        val listVehicleActivities = dbHelper.getUserVehicleActivities(user.id, startOfDay, endOfDay)
        val listStillActivities = dbHelper.getUserStillActivities(user.id, startOfDay, endOfDay)

        val listActivities = listWalkingActivities + listVehicleActivities + listStillActivities
        val sortedActivities = listActivities.sortedBy { it.timestamp }

        return sortedActivities
    }

    /**
     * Funzione attuata per definire il range temporale in cui ricercare le attività effettuate
     * dall'utente.
     */
    private fun getDayRange(dateStr: String?): Pair<String, String> {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(UNIVERSAL_DATE))

        val startOfDay = date.atStartOfDay()
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)

        val outputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }

    /**
     * Metodo definito per popolare la recycler view tramite la lista delle attività dell'utente.
     */
    private fun populateDailyActivitiesRecyclerView(activities: List<UserActivity>, dailyList: RecyclerView) {
        val adapterActivities = DailyActivityAdapter(activities)
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }
}
