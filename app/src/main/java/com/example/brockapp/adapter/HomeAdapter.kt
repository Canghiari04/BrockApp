package com.example.brockapp.adapter

import android.view.LayoutInflater
import com.example.brockapp.data.UserActivity

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class HomeAdapter(private val activities: List<UserActivity>): RecyclerView.Adapter<HomeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): HomeViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.home_activity_cell, parent, false)

        return HomeViewHolder(activityItem)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bindActivity(activities[position].type, "Data: ${activities[position].timestamp}")
    }

    override fun getItemCount(): Int {
        return activities.size
    }
}