package com.example.brockapp.calendar

import com.example.brockapp.R
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyActivityViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val activityImageView = itemView.findViewById<ImageView>(R.id.activity_cell_image)
    private val activityTitle = itemView.findViewById<TextView>(R.id.title_activity_text_view)
    private val activityTimestamp = itemView.findViewById<TextView>(R.id.time_activity_text_view)
    private val activityInfo = itemView.findViewById<TextView>(R.id.info_activity_text_view)

    fun bindActivity(typeActivity: String, timestampActivity: String?, infoActivity: String) {
        activityTitle.text = typeActivity
        activityTimestamp.text = timestampActivity
        activityInfo.text = infoActivity

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
    }
}