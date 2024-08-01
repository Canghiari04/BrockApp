package com.example.brockapp.calendar

import android.view.ViewGroup
import com.example.brockapp.R
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class ActivitiesAdapter(private val activities: ArrayList<String>) : RecyclerView.Adapter<ActivityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.activity_calendar_cell, parent, false)

        return ActivityViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bindActivity(activities[position])
    }
}