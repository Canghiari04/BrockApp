package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.R

import android.view.View
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class DailyActivityViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val activityDate = itemView.findViewById<TextView>(R.id.date_activity_text_view)
    private val activityInfo = itemView.findViewById<TextView>(R.id.info_activity_text_view)
    private val activityTitle = itemView.findViewById<TextView>(R.id.title_activity_text_view)
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)
    private val activityTimestamp = itemView.findViewById<TextView>(R.id.time_activity_text_view)

    fun bindActivity(typeActivity: String, dateActivity: String?, timestampActivity: String?, infoActivity: String) {
        activityTitle.text = typeActivity
        activityDate.text = dateActivity
        activityTimestamp.text = timestampActivity
        activityInfo.text = infoActivity

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