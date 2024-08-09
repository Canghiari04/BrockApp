package com.example.brockapp.calendar

import com.example.brockapp.R

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyActivityViewHolder(itemView: View, private val onItemClick: (Long, String) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val activityTextView = itemView.findViewById<TextView>(R.id.activity_cell_text)
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)

    fun bindActivity(activityId: Long, typeActivity: String) {
        activityTextView.text = typeActivity

        when(typeActivity) {
            "WALK" -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_walk_24)
            }
            "VEHICLE" -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_car_24)
            }
            "STILL" -> {
                activityImageView.setImageResource(R.drawable.baseline_chair_24)
            }
        }

        itemView.setOnClickListener {
            onItemClick(activityId, typeActivity)
        }
    }
}