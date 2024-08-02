package com.example.brockapp.calendar

import android.graphics.Color
import android.view.View
import com.example.brockapp.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarViewHolder(itemView: View, private val onItemClick: (String) -> Unit, private val showActivityOfDay: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
    // PER IL SELECTED ITEM FORSE SAREBBE DA USARE IL DAYOFMONTH PIUTTOSTO CHE L'ITEMVIEW
    var selectedItem: View? = null
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
            dayOfMonth.setBackgroundResource(R.color.white)
        } else {
            itemView.setOnClickListener{
                dayOfMonth.setBackgroundResource(R.color.uni_red)
                dayOfMonth.setTextColor(Color.parseColor("#FFFFFF"))
                onItemClick(date)
                showActivityOfDay(date)
            }
        }
    }
}