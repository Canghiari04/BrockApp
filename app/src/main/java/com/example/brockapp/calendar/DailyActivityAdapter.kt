package com.example.brockapp.calendar

import com.example.brockapp.R
import com.example.brockapp.data.UserActivity

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class DailyActivityAdapter(private val activities: List<UserActivity>) : RecyclerView.Adapter<DailyActivityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.activity_calendar_cell, parent, false)

        return DailyActivityViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    override fun onBindViewHolder(holder: DailyActivityViewHolder, position: Int) {
        holder.bindActivity(activities[position].type)
    }
}