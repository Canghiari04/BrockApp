package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.util.CalendarUtil
import com.example.brockapp.activity.DailyActivity
import com.example.brockapp.adapter.CalendarAdapter

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.content.Intent
import android.widget.TextView
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager

class CalendarFragment : Fragment(R.layout.calendar_fragment) {
    private val formatter = DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT)

    private lateinit var user: User
    private lateinit var utilCalendar: CalendarUtil

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = view.findViewById<RecyclerView>(R.id.calendar_recycler_view)

        user = User.getInstance()
        utilCalendar = CalendarUtil()

        setDate(LocalDate.now())
        populateCalendarRecyclerView(utilCalendar.getCurrentDays(LocalDate.now()), utilCalendar.getDates(LocalDate.now()), calendar)

        view.findViewById<ImageButton>(R.id.button_back_month).setOnClickListener {
            val tokens = (view.findViewById<TextView>(R.id.date_text_view).text).split(" ")

            var date = utilCalendar.getDateByTokens(formatter, tokens)
            date = date.minusMonths(1)
            date.format(formatter)

            setDate(date)
            populateCalendarRecyclerView(utilCalendar.getCurrentDays(date), utilCalendar.getDates(date), calendar)
        }

        view.findViewById<ImageButton>(R.id.button_forward_month).setOnClickListener {
            val tokens = (view.findViewById<TextView>(R.id.date_text_view).text).split(" ")

            var date = utilCalendar.getDateByTokens(formatter, tokens)
            date = date.plusMonths(1)
            date.format(formatter)

            setDate(date)
            populateCalendarRecyclerView(utilCalendar.getCurrentDays(date), utilCalendar.getDates(date), calendar)
        }
    }

    private fun setDate(date: LocalDate) {
        val strDate = "${date.month}" + " ${date.year}"
        view?.findViewById<TextView>(R.id.date_text_view)?.text = strDate.lowercase()
    }

    private fun populateCalendarRecyclerView(days: List<String>, dates: ArrayList<String>, calendar: RecyclerView) {
        val adapterCalendar = CalendarAdapter(days, dates, { date -> onItemClick(date) }, {date -> showActivityOfDay(date)})
        val layoutManager = GridLayoutManager(requireContext(), 7)

        calendar.adapter = adapterCalendar
        calendar.layoutManager = layoutManager
    }

    /**
     * Metodo associato alle singole ViewHolder per garantire la possibilità di impostare la data
     * corretta successivo all'evento click.
     */
    private fun onItemClick(date: String) {
        val tokens = date.split(DATE_SEPARATOR).toList()
        val item = LocalDate.of(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        setDate(item)
    }

    /**
     * Metodo attuato per attuare lo start di una nuova attività per visualizzare le informazioni
     * rispetto alla data corrente passata come parametro.
     */
    private fun showActivityOfDay(date: String) {
        val intent = Intent(requireContext(), DailyActivity::class.java).putExtra("ACTIVITY_DATE", date)
        startActivity(intent)
        activity?.finish()
    }
}