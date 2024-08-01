package com.example.brockapp.activity

import android.os.Bundle
import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDate
import com.example.brockapp.R
import android.widget.TextView
import android.widget.ImageButton
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.calendar.CalendarAdapter
import androidx.recyclerview.widget.GridLayoutManager

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        setDate(LocalDate.now())

        populateRecyclerView(getCurrentDays(LocalDate.now()), getDates(LocalDate.now()), findViewById(R.id.calendar_recycler_view))

        val buttonBack = findViewById<ImageButton>(R.id.button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.button_forward_month)

        buttonBack.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.date_text_view).text
            val tokens = strDate.split("/").toList()

            val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt() - 1, tokens[0].toInt())

            populateRecyclerView(getCurrentDays(date), getDates(date), findViewById(R.id.calendar_recycler_view))
            setDate(date)
        }

        buttonForward.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.date_text_view).text
            val tokens = strDate.split("/").toList()

            val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt() + 1, tokens[0].toInt())

            populateRecyclerView(getCurrentDays(date), getDates(date), findViewById(R.id.calendar_recycler_view))
            setDate(date)
        }
    }

    /**
     * Metodo attuato per modificare la visualizzazione grafica della data corrente.
     */
    private fun setDate(date: LocalDate) {
        findViewById<TextView>(R.id.date_text_view).text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
    }

    private fun populateRecyclerView(days: List<String>, dates: ArrayList<String>, calendar: RecyclerView) {
        val adapterCalendar = CalendarAdapter(days, dates) { date
            -> onItemClick(date)
        }
        val layoutManager = GridLayoutManager(this, 7)

        calendar.adapter = adapterCalendar
        calendar.layoutManager = layoutManager
    }

    /**
     * Funzione attuata per ottenere tutti i giorni di un determinato mese.
     */
    private fun getCurrentDays(date: LocalDate) : ArrayList<String> {
        var i = 0
        val list = getList(date)

        do {
            i++
            list.add(i.toString())
        } while(i < date.month.length(false))

        return list
    }

    /**
     * Funzione attuata per ottenere tutte le date complete da associare alle molteplici view holder.
     */
    private fun getDates(date: LocalDate) : ArrayList<String> {
        var i = 0
        var myDateId: String
        val list = getList(date)

        do {
            try {
                i++
                myDateId = i.toString() + "/" + date.monthValue.toString() + "/" + date.year.toString()
                list.add(myDateId)
            } catch (e: Exception) {
                Log.d("CALENDAR", e.toString())
            }
        } while(i < date.month.length(false))

        return list
    }

    private fun getList(date: LocalDate): ArrayList<String> {
        val firstDayOfMonth = LocalDate.of(date.year, date.month, 1)
        val startOfWeek = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val daysDistance = ChronoUnit.DAYS.between(startOfWeek, firstDayOfMonth)

        return ArrayList(MutableList(daysDistance.toInt()) {""})
    }

    fun onItemClick(date: String) {
        val tokens = date.split("/").toList()
        val item = LocalDate.of(tokens[2].toInt(), tokens[1].toInt(), tokens[0].toInt())
        setDate(item)
    }
}