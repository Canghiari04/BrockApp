package com.example.brockapp.calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class CalendarViewHolder(itemView: View, private val onItemClick: (String) -> Unit, private val showActivityOfDay: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val dayOfMonth = itemView.findViewById<TextView>(R.id.cell_day_text)

    /**
     *  Associazione dei parametri in input alla view holder in questione.
     *  Se l'id è empty la view holder non sarà cliccabile, altrimenti viene immesso il
     *  comportamento atteso all'interno del click listener.
     */
    fun bindDay(day: String, date: String) {
        dayOfMonth.text = day
        if(date.isEmpty()) {
            dayOfMonth.isClickable = false
            dayOfMonth.setBackgroundResource(R.color.grey)
        } else {
            itemView.setOnClickListener {
                onItemClick(date)
                showActivityOfDay(date)
            }
        }
    }
}