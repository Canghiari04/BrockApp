package com.example.brockapp.calendar

import com.example.brockapp.R

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val activityTextView = itemView.findViewById<TextView>(R.id.activity_cell_text)
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)

    fun bindActivity(nameActivity: String) {
        activityTextView.text = nameActivity

        when(nameActivity) {
            "Walk" -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_walk_24)
            }
            "Vehicle" -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_car_24)
            }
            "Still" -> {
                activityImageView.setImageResource(R.drawable.baseline_chair_24)
            }
        }
    }
}