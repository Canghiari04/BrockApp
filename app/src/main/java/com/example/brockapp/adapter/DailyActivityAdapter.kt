package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.data.UserActivity

import java.util.Locale
import android.view.ViewGroup
import java.text.SimpleDateFormat
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class DailyActivityAdapter(private val activities: List<UserActivity>) : RecyclerView.Adapter<DailyActivityViewHolder>() {
    private val filteredActivities: List<UserActivity> = activities.filter { it.transitionType == 1 }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.activity_cell, parent, false)

        return DailyActivityViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return filteredActivities.size
    }

    override fun onBindViewHolder(holder: DailyActivityViewHolder, position: Int) {
        val exitActivity = filteredActivities[position]
        val exitActivityTime = exitActivity.timestamp!!.split(" ")[1]

        // ?
        if (2 * position < activities.size) {
            val enterActivity = activities[(2 * position)]
            val enterActivityTime = enterActivity.timestamp!!.split(" ")[1]

            val timeDifferenceInSeconds = calculateTimeDifferenceInSeconds(enterActivityTime, exitActivityTime)

            when (exitActivity.type) {
                STILL_ACTIVITY_TYPE -> {
                    holder.bindActivity(
                        STILL_ACTIVITY_TYPE,
                        "Finito alle $exitActivityTime",
                        "Durata: $timeDifferenceInSeconds secondi"
                    )
                }

                VEHICLE_ACTIVITY_TYPE -> {
                    val distanceTravelled = String.format("%.2f", exitActivity.info)

                    holder.bindActivity(
                        VEHICLE_ACTIVITY_TYPE,
                        "Finito alle $exitActivityTime",
                        "Distanza percorsa: $distanceTravelled metri.\nDurata: $timeDifferenceInSeconds secondi"
                    )
                }

                WALK_ACTIVITY_TYPE -> {
                    holder.bindActivity(
                        WALK_ACTIVITY_TYPE,
                        "Finito alle $exitActivityTime",
                        "Passi fatti: ${exitActivity.info}. \nDurata: $timeDifferenceInSeconds secondi"
                    )
                }

                else -> {
                    holder.bindActivity(
                        exitActivity.type,
                        "Finito alle $exitActivityTime",
                        "\nDurata: $timeDifferenceInSeconds secondi"
                    )
                }
            }
        }
    }

    /**
     * Calcola la differenza di tempo in secondi tra due timestamp nel formato "HH:mm:ss".
     */
    private fun calculateTimeDifferenceInSeconds(startTime: String, endTime: String): Long {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val startDate = timeFormat.parse(startTime)
        val endDate = timeFormat.parse(endTime)

        return (endDate.time - startDate.time) / 1000
    }
}