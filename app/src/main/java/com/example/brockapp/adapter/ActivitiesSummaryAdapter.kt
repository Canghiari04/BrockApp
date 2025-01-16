package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.data.Activity
import com.example.brockapp.util.ScheduleWorkerUtil
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewModel.ActivitiesViewModel

import android.view.View
import java.time.LocalDate
import android.widget.Button
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.view.LayoutInflater
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView

class ActivitiesSummaryAdapter(private val scheduleWorkerUtil: ScheduleWorkerUtil, private val list: List<Activity>, private val viewModel: ActivitiesViewModel): RecyclerView.Adapter<ActivitiesSummaryAdapter.ActivityViewHolder>() {

    private val rangeUtil = PeriodRangeImpl()

    inner class ActivityViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_activity)
        val buttonDelete: Button = itemView.findViewById(R.id.button_delete_activity)
        val textViewType: TextView = itemView.findViewById(R.id.text_view_type_activity)
        val textViewDuration: TextView = itemView.findViewById(R.id.text_view_duration_activity)
        val textViewStartTimestamp: TextView = itemView.findViewById(R.id.text_view_start_time_stamp_activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.cell_activity, parent, false)
        return ActivityViewHolder(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = list[position]

        val pair = defineImageAndTable(item.type)

        holder.imageView.setImageResource(pair.first)

        holder.textViewType.text = item.type
        holder.textViewDuration.text = item.duration
        holder.textViewStartTimestamp.text = item.timestamp

        holder.buttonDelete.setOnClickListener {
            val dateTime = LocalDate.parse(
                item.timestamp,
                DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)
            )

            val range = rangeUtil.getDayRange(dateTime)

            viewModel.deleteActivity(item.id, item.type)
            viewModel.getAllActivities(range.first, range.second)

            scheduleWorkerUtil.scheduleDeleteActivityWorker(item.id, pair.second)
        }
    }

    private fun defineImageAndTable(type: String): Pair<Int, String> {
        return when {
            type.contains("Vehicle") -> {
                Pair(R.drawable.icon_car, "UsersVehicleActivity")
            }

            type.contains("Run") -> {
                Pair(R.drawable.icon_run, "UsersRunActivity")
            }

            type.contains("Still") -> {
                Pair(R.drawable.icon_still, "UsersStillActivity")
            }

            else -> {
                Pair(R.drawable.icon_walk, "UsersWalkActivity")
            }
        }
    }
}