package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.data.UserActivity

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class FriendActivitiesAdapter(activities: List<UserActivity>): RecyclerView.Adapter<FriendActivitiesViewHolder>() {
    private val filteredActivities: List<UserActivity> = activities.filter { it.transitionType == 1 }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): FriendActivitiesViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_friend_activity, parent, false)
        return FriendActivitiesViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return filteredActivities.size
    }

    override fun onBindViewHolder(holder: FriendActivitiesViewHolder, position: Int) {
        val timestamp = filteredActivities[position].timestamp
        val tokens = timestamp?.split(" ")

        val date = tokens?.get(0)

        val exitActivity = filteredActivities[position]

        when (exitActivity.type) {
            STILL_ACTIVITY_TYPE -> {
                holder.bindActivity(
                    STILL_ACTIVITY_TYPE,
                    "Data $date"
                )
            }

            VEHICLE_ACTIVITY_TYPE -> {
                holder.bindActivity(
                    VEHICLE_ACTIVITY_TYPE,
                    "Data $date",
                )
            }

            WALK_ACTIVITY_TYPE -> {
                holder.bindActivity(
                    WALK_ACTIVITY_TYPE,
                    "Data $date"
                )
            }

            else -> {
                return
            }
        }
    }
}