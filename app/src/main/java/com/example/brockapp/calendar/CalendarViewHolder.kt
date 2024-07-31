package com.example.brockapp.calendar

import android.view.View
import java.time.LocalDate
import com.example.brockapp.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dayOfMonth = itemView.findViewById<TextView>(R.id.cell_day_text)

    // Variabile per tenere traccia dell'elemento selezionato
    private var selectedItem: View? = null

    fun bindDay(day: String, date: LocalDate) {
        itemView.id = convertDateToInt(date)
        dayOfMonth.text = day.toString()

        itemView.setOnClickListener {
            // Rimuove il bordo dall'elemento precedentemente selezionato
            selectedItem?.setBackgroundResource(0)
            // Imposta il bordo rosso all'elemento attualmente selezionato
            itemView.setBackgroundResource(R.drawable.border_red)
            // Aggiorna l'elemento selezionato
            selectedItem = itemView
        }
    }

    /**
     * Funzione che converte una data in un intero, sommando anno, mese e giorno. Ottenendo un id univoco.
     */
    private fun convertDateToInt(date: LocalDate): Int {
        return date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
    }
}
