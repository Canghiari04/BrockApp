package com.example.brockapp.calendar

import android.view.View
import java.time.LocalDate
import com.example.brockapp.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dayOfMonth = itemView.findViewById<TextView>(R.id.cell_day_text)

    fun bindDay(day: Int, id: LocalDate) {
        itemView.id = convertDateToInt(id)
        dayOfMonth.text = day.toString()
    }

    /**
     * Funzione che converte una date in un intero, sommando anno, mese e giorno. Ottenendo un id univoco.
     */
    private fun convertDateToInt(date: LocalDate) : Int {
        return date.year + date.monthValue + date.dayOfMonth
    }
}