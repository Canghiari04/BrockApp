package com.example.brockapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener

class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_fragment_container)

        val calendarView = findViewById<MaterialCalendarView>(R.id.calendar_main_content)

        calendarView.setOnDateChangedListener { widget, date, selected ->

            Toast.makeText(this, "Data selezionata: ${date.date}", Toast.LENGTH_SHORT).show()
        }
    }
}
