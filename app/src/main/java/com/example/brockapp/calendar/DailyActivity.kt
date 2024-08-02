package com.example.brockapp.calendar

import UserActivity
import com.example.brockapp.R

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.activity.CalendarActivity
import com.example.brockapp.database.DbHelper
import java.time.LocalDate

class DailyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val dbHelper : DbHelper = DbHelper(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
        val textView = findViewById<TextView>(R.id.date_text_view)

        val date : String? = intent.getStringExtra("ACTIVITY_DATE")

        val (startOfDay, endOfDay) = dbHelper.getDayRange(date)
        val listActivityWalk = dbHelper.getUserWalkActivities(CalendarActivity.user.id, startOfDay, endOfDay)
        val listActivityVehicle = dbHelper.getUserVehicleActivities(CalendarActivity.user.id, startOfDay, endOfDay)
        val listActivityStill = dbHelper.getUserStillActivities(CalendarActivity.user.id, startOfDay, endOfDay)

        val userActivityList : List<UserActivity> = listActivityWalk + listActivityStill + listActivityVehicle

        val timestamps: List<String> = userActivityList.map { it.timestamp }
        val walkActivityCount = listActivityWalk.size
        val vehicleActivityCount = listActivityVehicle.size
        val stillActivityCount = listActivityStill.size

        // RICAVARE IL MESE IN STRINGA
        textView.text = date

    }

    private fun populateDailyActivitiesRecyclerView(activities: ArrayList<String>, dailyList: RecyclerView) {
        val adapterActivities = DailyActivityAdapter(activities)
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }
}
