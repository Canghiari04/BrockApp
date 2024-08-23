package com.example.brockapp.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class CalendarViewHolder(itemView: View, private val onItemClick: (String) -> Unit, private val showActivityOfDay: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
    private val dayOfMonth = itemView.findViewById<TextView>(R.id.cell_day_text)

    fun bindDay(day: String, date: String) {
        dayOfMonth.text = day

        if(date.isEmpty()) {
            dayOfMonth.isClickable = false
        } else {
            itemView.setOnClickListener {
                onItemClick(date)
                showActivityOfDay(date)
            }
        }
    }
}