package com.example.brockapp.calendar

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.DATE_FORMAT
import com.example.brockapp.UNIVERSAL_DATE
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity

import android.os.Bundle
import java.time.LocalDate
import android.widget.TextView
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import java.time.format.DateTimeFormatter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brockapp.ISO_DATE_FORMAT

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

        lifecycleScope.launch(Dispatchers.IO) {
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

        // TODO -> TROVARE IL MODO PER MIGLIORARE QUESTA SCHIFEZZA
        for(activity in listStillActivities) {
            val newActivity = UserActivity(activity.id, activity.userId, activity.timestamp, "STILL")
            listActivities.add(newActivity)
        }

        for(activity in listVehicleActivities) {
            val newActivity = UserActivity(activity.id, activity.userId, activity.timestamp, "VEHICLE")
            listActivities.add(newActivity)
        }

        for(activity in listWalkingActivities) {
            val newActivity = UserActivity(activity.id, activity.userId, activity.timestamp, "WALK")
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
        val adapterActivities = DailyActivityAdapter(activities, {activityId, type -> onItemClick(activityId, type)})
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }

    private fun onItemClick(activityId: Long, type: String) {

        // PRENDERE IL DIALOG
        // PASSARGLI I DATI
        // DIALOG INTERNAMENTE QUERY AL DB ROOM
        // ASSOCIAZIONE DEI DATI ESTRAPOLATI NELLA VIEW
    }
}
