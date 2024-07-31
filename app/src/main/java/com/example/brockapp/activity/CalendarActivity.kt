package com.example.brockapp.activity

import android.os.Bundle
import android.util.Log
import java.time.LocalDate
import com.example.brockapp.R
import android.widget.TextView
import android.widget.ImageButton
import java.time.format.DateTimeFormatter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.calendar.CalendarAdapter
import androidx.recyclerview.widget.GridLayoutManager

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        setDate(LocalDate.now())

        val buttonBack = findViewById<ImageButton>(R.id.button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.button_forward_month)

        populateRecyclerView(getCurrentDays(LocalDate.now()), getIdsByDate(LocalDate.now()), findViewById(R.id.calendar_recycler_view))

        buttonBack.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.date_text_view).text
            val tokens = strDate.split("/").toList()

            val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt() - 1, 1)

            populateRecyclerView(getCurrentDays(date), getIdsByDate(date), findViewById(R.id.calendar_recycler_view))
            setDate(date)
        }

        buttonForward.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.date_text_view).text
            val tokens = strDate.split("/").toList()

            val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt() + 1, 1)

            populateRecyclerView(getCurrentDays(date), getIdsByDate(date), findViewById(R.id.calendar_recycler_view))
            setDate(date)
        }
    }

    private fun populateRecyclerView(days: List<Int>, ids: ArrayList<LocalDate>, calendar: RecyclerView) {
        val adapterCalendar = CalendarAdapter(days, ids)
        val layoutManager = GridLayoutManager(this, 7)

        calendar.adapter = adapterCalendar
        calendar.layoutManager = layoutManager
    }

    /**
     * Funzione attuata per ottenere tutti i giorni di un determinato mese.
     */
    private fun getCurrentDays(date: LocalDate) : List<Int> {
        return (1..date.lengthOfMonth()).toList()
    }

    private fun getIdsByDate(date: LocalDate) : ArrayList<LocalDate> {
        var i = 0
        var myDateId: LocalDate
        var listDateIds = ArrayList<LocalDate>()

        do {
            try {
                i++
                myDateId = LocalDate.of(date.year, date.month, i)
                listDateIds.add(myDateId)
            } catch (e: Exception) {
                Log.d("CALENDAR", e.toString())
            }
        } while(i < date.month.length(false))

        return listDateIds
    }

    private fun setDate(date: LocalDate) {
        findViewById<TextView>(R.id.date_text_view).text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
    }
}