package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.database.DbHelper
import com.example.brockapp.calendar.CalendarAdapter
import com.example.brockapp.calendar.ActivitiesAdapter

import android.os.Bundle
import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDate
import android.widget.TextView
import android.widget.ImageButton
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brockapp.DATE_FORMAT
import com.example.brockapp.DATE_SEPARATOR
import java.text.SimpleDateFormat


class CalendarActivity : AppCompatActivity() {
    val dbHelper = DbHelper(this)

    companion object {
        val user: User = User.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        setDate(LocalDate.now())

        populateCalendarRecyclerView(getCurrentDays(LocalDate.now()), getDates(LocalDate.now()), findViewById(R.id.calendar_recycler_view))

        val buttonBack = findViewById<ImageButton>(R.id.button_back_month)
        val buttonForward = findViewById<ImageButton>(R.id.button_forward_month)

        buttonBack.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.date_text_view).text
            val tokens = strDate.split(DATE_SEPARATOR).toList()

            val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt() - 1, tokens[0].toInt())

            populateCalendarRecyclerView(getCurrentDays(date), getDates(date), findViewById(R.id.calendar_recycler_view))
            setDate(date)
        }

        buttonForward.setOnClickListener {
            val strDate = findViewById<TextView>(R.id.date_text_view).text
            val tokens = strDate.split(DATE_SEPARATOR).toList()

            val date = LocalDate.of(tokens[2].toInt(), tokens[1].toInt() + 1, tokens[0].toInt())

            populateCalendarRecyclerView(getCurrentDays(date), getDates(date), findViewById(R.id.calendar_recycler_view))
            setDate(date)
        }
    }

    /**
     * Metodo attuato per modificare la visualizzazione grafica della data corrente.
     */
    private fun setDate(date: LocalDate) {
        findViewById<TextView>(R.id.date_text_view).text = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString()
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

        do {
            try {
                i++
                myDateId = i.toString() + DATE_SEPARATOR + date.monthValue.toString() + DATE_SEPARATOR + date.year.toString()
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

    private fun onItemClick(date: String) {
        val tokens = date.split(DATE_SEPARATOR ).toList()
        val item = LocalDate.of(tokens[2].toInt(), tokens[1].toInt(), tokens[0].toInt())

        setDate(item)
    }

    private fun showActivityOfDay(date: String) {


        val (startOfDay, endOfDay) = dbHelper.getDayRange(date)
        val listActivityWalk = dbHelper.getUserWalkActivities(user.id, startOfDay, endOfDay)
        val listActivityVehicle = dbHelper.getUserVehicleActivities(user.id, startOfDay, endOfDay)
        val listActivityStill = dbHelper.getUserStillActivities(user.id, startOfDay, endOfDay)

        //val activityList = listActivityVehicle + listActivityStill + listActivityWalk

        val dateFormat = SimpleDateFormat(DATE_FORMAT)

        // Converte le stringhe in oggetti Date e sortale in ordine cronologico inverso
//        val sortedList = activityList.map { dateFormat.parse(it) to it }
//            .sortedByDescending { it.first }
//            .map { it.second }


        // USATA SOLO COME PROVA, DOVREMMO CREARE UNA LISTA CHE COMBINA LE ATTIVITÀ IN ORDINE CRONOLOGICO DAI GET PRECEDENTI.
        populateActivitiesRecyclerView(ArrayList(MutableList(listActivityWalk.size) {"Walk"}), findViewById(R.id.activities_recycler_view))
    }

    private fun populateActivitiesRecyclerView(activities: ArrayList<String>, list: RecyclerView) {
        val adapterActivities = ActivitiesAdapter(activities)
        val layoutManager = LinearLayoutManager(this)

        list.adapter = adapterActivities
        list.layoutManager = layoutManager
    }
}