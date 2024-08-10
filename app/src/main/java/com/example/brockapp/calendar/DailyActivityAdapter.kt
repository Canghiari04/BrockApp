package com.example.brockapp.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.data.UserActivity

class DailyActivityAdapter(private val activities: List<UserActivity>, private val onItemClick: (Long, String) -> Unit) : RecyclerView.Adapter<DailyActivityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.activity_calendar_cell, parent, false)

        return DailyActivityViewHolder(activityItem, onItemClick)
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    override fun onBindViewHolder(holder: DailyActivityViewHolder, position: Int) {
        holder.bindActivity(activities[position].activityId, activities[position].type)
    }
}