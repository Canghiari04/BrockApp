package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.R

import android.view.View
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FriendActivitiesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val activityType = itemView.findViewById<TextView>(R.id.type_activity_text_view)
    private val activityDate = itemView.findViewById<TextView>(R.id.date_activity_text_view)
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)

    fun bindActivity(typeActivity: String, dateActivity: String?) {
        activityType.text = typeActivity
        activityDate.text = dateActivity

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