package com.example.brockapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.data.UserActivity

class DailyActivityAdapter(private val activities: List<UserActivity>): RecyclerView.Adapter<DailyActivityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.activity_cell, parent, false)

        return DailyActivityViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    override fun onBindViewHolder(holder: DailyActivityViewHolder, position: Int) {
        val time = activities[position].timestamp!!.split(" ")[1]

        when(activities[position].type) {
            WALK_ACTIVITY_TYPE -> {
                holder.bindActivity(WALK_ACTIVITY_TYPE, "Finito alle " + time, "Passi fatti: " + activities[position].info)
            }
            VEHICLE_ACTIVITY_TYPE-> {
                holder.bindActivity(VEHICLE_ACTIVITY_TYPE, "Finito alle " + time, "Distanza percorsa: " + activities[position].info + " metri")
            }
            else -> {
                holder.bindActivity(activities[position].type, "Finito alle " + time, activities[position].info)
            }
        }
    }
}