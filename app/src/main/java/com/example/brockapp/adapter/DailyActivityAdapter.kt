package com.example.brockapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.data.UserActivity
import java.text.SimpleDateFormat
import java.util.Locale

class DailyActivityAdapter(private val activities: List<UserActivity>): RecyclerView.Adapter<DailyActivityViewHolder>() {
    private val filteredActivities: List<UserActivity> = activities.filter { it.transitionType == 1 }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_activity, parent, false)
        return DailyActivityViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return filteredActivities.size
    }

    override fun onBindViewHolder(holder: DailyActivityViewHolder, position: Int) {
        val timestamp = activities[position].timestamp
        val tokens = timestamp?.split(" ")

        val date = tokens?.get(0)

        val exitActivity = filteredActivities[position]
        val exitActivityTime = exitActivity.timestamp!!.split(" ")[1]

        if (2 * position < activities.size) {
            val enterActivity = activities[(2 * position)]
            val enterActivityTime = enterActivity.timestamp!!.split(" ")[1]

            val timeDifference = calculateTimeDifference(enterActivityTime, exitActivityTime)

            when (exitActivity.type) {
                STILL_ACTIVITY_TYPE -> {
                    holder.bindActivity(
                        STILL_ACTIVITY_TYPE,
                        "Data: $date",
                        "Terminata alle $exitActivityTime",
                        "Durata: $timeDifference"
                    )
                }

                VEHICLE_ACTIVITY_TYPE -> {
                    val distanceTravelled = exitActivity.info.split(".")[0]

                    holder.bindActivity(
                        VEHICLE_ACTIVITY_TYPE,
                        "Data: $date",
                        "Terminata alle $exitActivityTime",
                        "Distanza percorsa: $distanceTravelled metri\nDurata: $timeDifference"
                    )
                }

                WALK_ACTIVITY_TYPE -> {
                    holder.bindActivity(
                        WALK_ACTIVITY_TYPE,
                        "Data: $date",
                        "Terminata alle $exitActivityTime",
                        "Passi fatti: ${exitActivity.info}\nDurata: $timeDifference"
                    )
                }

                else -> {
                    return
                }
            }
        }
    }

    private fun calculateTimeDifference(startTime: String, endTime: String): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val startDate = timeFormat.parse(startTime)
        val endDate = timeFormat.parse(endTime)
        val timeDifference = ((endDate.time - startDate.time) / 1000)

        if (timeDifference >= 60) {
            val hour = (timeDifference / 3600).toInt()
            val minute = (timeDifference / 60).toInt()
            val second = (timeDifference % 60).toInt()

            if (hour > 0){
                return "$hour ore, $minute minuti, $second secondi"
            }

            return "$minute minuti, $second secondi"
        } else {
            return "$timeDifference secondi"
        }
    }
}