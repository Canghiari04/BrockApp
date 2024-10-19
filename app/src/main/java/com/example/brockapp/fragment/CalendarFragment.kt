package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.util.CalendarUtil
import com.example.brockapp.adapter.CalendarAdapter
import com.example.brockapp.activity.DailyMemoActivity

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Button
import android.content.Intent
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager

class CalendarFragment: Fragment(R.layout.fragment_calendar) {
    private val util = CalendarUtil()
    private val formatter = DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDate(LocalDate.now())

        val calendar = view.findViewById<RecyclerView>(R.id.recycler_view_calendar)
        populateCalendarRecyclerView(util.getCurrentDays(LocalDate.now()), util.getDates(LocalDate.now()), calendar)

        val buttonBack = view.findViewById<Button>(R.id.button_back_month)
        val buttonForward = view.findViewById<Button>(R.id.button_forward_month)

        setUpButton(buttonBack, buttonForward, calendar)
    }

    private fun setDate(date: LocalDate) {
        view?.findViewById<TextView>(R.id.date_text_view)?.apply {
            text = "${date.month.toString().toLowerCase()}, ${date.year}"
        }
    }

    private fun populateCalendarRecyclerView(days: List<String>, dates: ArrayList<String>, calendar: RecyclerView) {
        val adapterCalendar = CalendarAdapter(days, dates, { date -> onItemClick(date) }, {date -> showMemos(date)})
        val layoutManager = GridLayoutManager(requireContext(), 7)

        calendar.adapter = adapterCalendar
        calendar.layoutManager = layoutManager
    }

    private fun setUpButton(buttonBack: Button, buttonForward: Button, calendar: RecyclerView) {
        buttonBack.setOnClickListener {
            val tokens = (view?.findViewById<TextView>(R.id.date_text_view)?.text)?.split(DATE_TEXT_VIEW)

            val date = util.getDateByTokens(formatter, tokens!!).minusMonths(1)
            date.format(formatter)

            setDate(date)
            populateCalendarRecyclerView(util.getCurrentDays(date), util.getDates(date), calendar)
        }

        buttonForward.setOnClickListener {
            val tokens = (view?.findViewById<TextView>(R.id.date_text_view)?.text)?.split(DATE_TEXT_VIEW)

            val date = util.getDateByTokens(formatter, tokens!!).plusMonths(1)
            date.format(formatter)

            setDate(date)
            populateCalendarRecyclerView(util.getCurrentDays(date), util.getDates(date), calendar)
        }
    }

    private fun onItemClick(date: String) {
        val tokens = date.split(DATE_SEPARATOR).toList()
        val item = LocalDate.of(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        setDate(item)
    }

    private fun showMemos(date: String) {
        val intent = Intent(requireContext(), DailyMemoActivity::class.java).putExtra("CALENDAR_DATE", date)
        startActivity(intent)
        activity?.finish()
    }
}