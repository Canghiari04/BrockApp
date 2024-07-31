package com.example.brockapp.activity

import android.os.Bundle
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.calendar.CalendarAdapter
import androidx.recyclerview.widget.GridLayoutManager

class CalendarActivity : AppCompatActivity() {
    private var currentDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        setDate(currentDate)

        val buttonBack = findViewById<ImageButton>(R.id.button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.button_forward_month)
        val calendarRecyclerView = findViewById<RecyclerView>(R.id.calendar_recycler_view)

        populateRecyclerView(currentDate, calendarRecyclerView)

        buttonBack.setOnClickListener {
            currentDate = currentDate.minusMonths(1)
            populateRecyclerView(currentDate, calendarRecyclerView)
            setDate(currentDate)
        }

        buttonForward.setOnClickListener {
            currentDate = currentDate.plusMonths(1)
            populateRecyclerView(currentDate, calendarRecyclerView)
            setDate(currentDate)
        }
    }

    private fun populateRecyclerView(date: LocalDate, calendar: RecyclerView) {
        val days = getCurrentDaysWithEmpty(date)
        val ids = getIdsByDate(date)
        val adapterCalendar = CalendarAdapter(days, ids)
        val layoutManager = GridLayoutManager(this, 7) // 7 colonne per i giorni della settimana

        calendar.adapter = adapterCalendar
        calendar.layoutManager = layoutManager
    }

    /**
     * Funzione attuata per ottenere tutti i giorni di un determinato mese, con spazi vuoti
     * per allineare il primo giorno del mese con il giorno della settimana corretto.
     */
    private fun getCurrentDaysWithEmpty(date: LocalDate): List<String> {
        val daysInMonth = date.lengthOfMonth()
        val firstDayOfWeek = date.withDayOfMonth(1).dayOfWeek.value % 7 // 0 = Domenica, 1 = Lunedì, ..., 6 = Sabato
        val days = mutableListOf<String>()

        // Aggiungi spazi vuoti per i giorni della settimana precedenti il primo giorno del mese
        for (i in 0 until firstDayOfWeek) {
            days.add("")
        }

        // Aggiungi i giorni del mese
        for (day in 1..daysInMonth) {
            days.add(day.toString())
        }

        return days
    }

    /**
     * Genera una lista di LocalDate per ogni giorno del mese.
     */
    private fun getIdsByDate(date: LocalDate): ArrayList<LocalDate> {
        val daysInMonth = date.lengthOfMonth()
        val ids = ArrayList<LocalDate>(daysInMonth)

        for (day in 1..daysInMonth) {
            ids.add(LocalDate.of(date.year, date.month, day))
        }

        return ids
    }

    private fun setDate(date: LocalDate) {
        findViewById<TextView>(R.id.date_text_view).text = date.format(DateTimeFormatter.ofPattern("MM/yyyy"))
    }
}
