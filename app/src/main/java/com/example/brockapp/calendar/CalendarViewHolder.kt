package com.example.brockapp.calendar

import android.view.View
import java.time.LocalDate
import com.example.brockapp.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.selects.select

class CalendarViewHolder(itemView: View, private val onItemClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
    var selectedItem: View? = null
    val dayOfMonth = itemView.findViewById<TextView>(R.id.cell_day_text)

    /**
     *  Associazione dei parametri in input alla view holder in questione.
     *  Se l'id è empty la view holder non sarà cliccabile, altrimenti viene immesso il
     *  comportamento atteso all'interno del click listener.
     */
    fun bindDay(day: String, date: String) {
        dayOfMonth.text = day
        if(date.isEmpty()) {
            itemView.isClickable = false
        } else {
            itemView.setOnClickListener{
                onItemClick(date)
            }
        }
    }
}