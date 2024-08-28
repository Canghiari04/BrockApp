package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.UserActivity

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(private val activities: List<UserActivity>): RecyclerView.Adapter<HomeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): HomeViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.home_activity_cell, parent, false)

        return HomeViewHolder(activityItem)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val timestamp = activities[position].timestamp
        val tokens = timestamp?.split(" ")

        val date = tokens?.get(0)
        val time = tokens?.get(1)

        holder.bindActivity(activities[position].type, "Finito in data $date alle $time")
    }

    override fun getItemCount(): Int {
        return activities.size
    }
}