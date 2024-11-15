package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.R

import android.view.View
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class HomeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)
    private val activityTitle = itemView.findViewById<TextView>(R.id.title_activity_text_view)
    private val activityTimestamp = itemView.findViewById<TextView>(R.id.time_activity_text_view)

    fun bindActivity(typeActivity: String, timestampActivity: String?) {
        activityTitle.text = typeActivity
        activityTimestamp.text = timestampActivity

        when (typeActivity) {
            STILL_ACTIVITY_TYPE -> {
                activityImageView.setImageResource(R.drawable.baseline_chair_24)
            }

            VEHICLE_ACTIVITY_TYPE -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_car_24)
            }

            WALK_ACTIVITY_TYPE -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_walk_24)
            }
        }
    }
}