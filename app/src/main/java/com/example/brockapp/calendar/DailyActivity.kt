package com.example.brockapp.calendar

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.DATE_FORMAT
import com.example.brockapp.UNIVERSAL_DATE
import com.example.brockapp.database.DbHelper
import com.example.brockapp.data.UserActivity

import android.os.Bundle
import java.time.LocalDate
import android.widget.TextView
import java.time.format.DateTimeFormatter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class DailyActivity : AppCompatActivity() {
    private val dbHelper : DbHelper = DbHelper(this)

    companion object {
        val user = User.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val date : String? = intent.getStringExtra("ACTIVITY_DATE")

        val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
        val textView = findViewById<TextView>(R.id.date_text_view)
        textView.text = date

        val sortedActivities = getUserActivities(date)

        populateDailyActivitiesRecyclerView(sortedActivities, dailyList)
    }

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
