package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val days: List<String>, private val dates: ArrayList<String>, private val onItemClick: (String) -> Unit, private val showActivityOfDay: (String) -> Unit): RecyclerView.Adapter<CalendarViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CalendarViewHolder {
        val dayItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_calendar, parent, false)

        return CalendarViewHolder(dayItem, onItemClick, showActivityOfDay)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bindDay(days[position], dates[position])
    }

    override fun getItemCount(): Int {
        return days.size
    }
}