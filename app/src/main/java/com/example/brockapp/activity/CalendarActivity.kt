package com.example.brockapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.CALENDAR_DATE_FORMAT
import com.example.brockapp.DATE_SEPARATOR
import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.calendar.CalendarAdapter
import com.example.brockapp.calendar.DailyActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class CalendarActivity : AppCompatActivity() {
    private val formatter = DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT)

    companion object {
        val user: User = User.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        setDate(LocalDate.of(2024, 8, 31))

        val calendar = findViewById<RecyclerView>(R.id.calendar_recycler_view)

        populateCalendarRecyclerView(getCurrentDays(LocalDate.now()), getDates(LocalDate.now()), calendar)

        val buttonBack = findViewById<ImageButton>(R.id.button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.button_forward_month)

        buttonBack.setOnClickListener {
            val tokens = (findViewById<TextView>(R.id.date_text_view).text).split(" ")

            var date = getDateByTokens(tokens)
            date = date.minusMonths(1)
            date.format(formatter)

            populateCalendarRecyclerView(getCurrentDays(date), getDates(date), calendar)
            setDate(date)
        }

        buttonForward.setOnClickListener {
            val tokens = (findViewById<TextView>(R.id.date_text_view).text).split(" ")

            var date = getDateByTokens(tokens)
            date = date.plusMonths(1)
            date.format(formatter)

            populateCalendarRecyclerView(getCurrentDays(date), getDates(date), calendar)
            setDate(date)
        }
    }

    /**
     * Metodo attuato per modificare la visualizzazione grafica della data corrente.
     */
    private fun setDate(date: LocalDate) {
        val strDate = "${date.month}" + " ${date.year}"
        findViewById<TextView>(R.id.date_text_view).text = strDate.lowercase()
    }

    private fun populateCalendarRecyclerView(days: List<String>, dates: ArrayList<String>, calendar: RecyclerView) {
        val adapterCalendar = CalendarAdapter(days, dates, { date -> onItemClick(date) }, {date -> showActivityOfDay(date)})
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

        try {
            do {
                i++
                myDateId = date.withDayOfMonth(i).toString()
                list.add(myDateId)

            } while(i < date.month.length(false))
        } catch (e: Exception) {
            Log.d("CALENDAR", e.toString())
        }

        return list
    }

    private fun getList(date: LocalDate): ArrayList<String> {
        val firstDayOfMonth = LocalDate.of(date.year, date.month, 1)
        val startOfWeek = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val daysDistance = ChronoUnit.DAYS.between(startOfWeek, firstDayOfMonth)

        return ArrayList(MutableList(daysDistance.toInt()) {""})
    }

    /**
     * Funzione attuata per ricavare l'ultimo giorno del mese. La LocalDate restituita è utilizzata
     * per navigare tra i mesi dell'anno circoscritto.
     */
    private fun getDateByTokens(tokens: List<String>): LocalDate {
        val year = tokens[1].toInt()
        val month = Month.valueOf(tokens[0].uppercase()).value
        val lastDay = YearMonth.of(year, month).atEndOfMonth()

        return LocalDate.parse(lastDay.toString(), formatter)
    }

    private fun onItemClick(date: String) {
        val tokens = date.split(DATE_SEPARATOR).toList()
        val item = LocalDate.of(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        setDate(item)
    }

    private fun showActivityOfDay(date: String) {
        val intent = Intent(this, DailyActivity::class.java).putExtra("ACTIVITY_DATE", date)

        startActivity(intent)
    }
}