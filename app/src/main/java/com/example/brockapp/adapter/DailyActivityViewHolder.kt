package com.example.brockapp.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.WALK_ACTIVITY_TYPE

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