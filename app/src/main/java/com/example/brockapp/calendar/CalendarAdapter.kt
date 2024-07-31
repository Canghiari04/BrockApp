package com.example.brockapp.calendar

import android.view.ViewGroup
import com.example.brockapp.R
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

/**
 * Classe che associa i dati alla visualizzazione grafica.
 */
class CalendarAdapter(var days: List<Int>, var ids: ArrayList<LocalDate>) : RecyclerView.Adapter<CalendarViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CalendarViewHolder {
        val dayItem = LayoutInflater.from(parent.context).inflate(R.layout.calendar_cell_activity, parent, false)
        return CalendarViewHolder(dayItem)
    }

    override fun getItemCount(): Int {
        return days.size
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bindDay(days[position], ids[position])
    }
}