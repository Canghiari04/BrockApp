package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.UserActivity

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class ActivityAdapter(private val activities: List<UserActivity>): RecyclerView.Adapter<ActivityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_activity, parent, false)
        return ActivityViewHolder(activityItem)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]

        val timestamp = activity.timestamp
        val tokens = timestamp?.split(" ")

        val date = tokens?.get(0)
        val time = tokens?.get(1)

        holder.bindActivity(activity.type, "Finished at $date $time")
    }

    override fun getItemCount(): Int {
        return activities.size
    }
}