package com.example.brockapp.calendar

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.WALK_ACTIVITY_TYPE

class DailyActivityViewHolder(itemView: View, private val onItemClick: (Long, String) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val activityTextView = itemView.findViewById<TextView>(R.id.activity_cell_text)
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)

    fun bindActivity(activityId: Long, typeActivity: String) {
        activityTextView.text = typeActivity

        when(typeActivity) {
            WALK_ACTIVITY_TYPE -> {
                activityImageView.setImageResource(R.drawable.baseline_directions_walk_24)
            }
            VEHICLE_ACTIVITY_TYPE-> {
                activityImageView.setImageResource(R.drawable.baseline_directions_car_24)
            }
            STILL_ACTIVITY_TYPE -> {
                activityImageView.setImageResource(R.drawable.baseline_chair_24)
            }
        }

        itemView.setOnClickListener {
            onItemClick(activityId, typeActivity)
        }
    }
}