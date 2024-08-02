package com.example.brockapp.calendar

import com.example.brockapp.R

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class DailyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_activity_activity)

        val dailyList = findViewById<RecyclerView>(R.id.activities_recycler_view)
        val textView = findViewById<TextView>(R.id.date_text_view)

        val date = intent.getStringExtra("ACTIVITY_DATE")
        val activities = intent.getStringArrayListExtra("ACTIVITIES_LIST")

        // RICAVARE IL MESE IN STRINGA
        textView.text = date

        if (activities != null) populateDailyActivitiesRecyclerView(activities, dailyList) else Log.d("DAILY_ACTIVITY", "Errore nel passaggio dell'intent.")
    }

    private fun populateDailyActivitiesRecyclerView(activities: ArrayList<String>, dailyList: RecyclerView) {
        val adapterActivities = DailyActivityAdapter(activities)
        val layoutManager = LinearLayoutManager(this)

        dailyList.adapter = adapterActivities
        dailyList.layoutManager = layoutManager
    }
}
