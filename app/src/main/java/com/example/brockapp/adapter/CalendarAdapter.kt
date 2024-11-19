package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val days: List<String>, private val dates: ArrayList<String>, private val onItemClick: (String) -> Unit, private val showActivityOfDay: (String) -> Unit): RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(itemView: View, private val onItemClick: (String) -> Unit, private val showActivityOfDay: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
        val day: TextView = itemView.findViewById(R.id.cell_day_text)

        fun setupViewHolder(date: String) {
            if (date.isBlank()) {
                itemView.visibility = View.GONE
            } else {
                itemView.setOnClickListener {
                    onItemClick(date)
                    showActivityOfDay(date)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CalendarViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.cell_calendar, parent, false)
        return CalendarViewHolder(item, onItemClick, showActivityOfDay)
    }

    override fun getItemCount(): Int {
        return days.size
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = days[position]

        holder.day.text = item
        holder.setupViewHolder(dates[position])
    }
}