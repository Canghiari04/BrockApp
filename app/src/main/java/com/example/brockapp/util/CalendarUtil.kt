package com.example.brockapp.util

import com.example.brockapp.*

import java.util.Locale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class CalendarUtil {
    fun getDateByTokens(formatter: DateTimeFormatter, tokens: List<String>): LocalDate {
        val year = tokens[1].toInt()
        val month = tokens[0].toInt()
        val lastDay = YearMonth.of(year, month).atEndOfMonth()

        return LocalDate.parse(lastDay.toString(), formatter)
    }

    fun getCurrentDays(date: LocalDate): ArrayList<String> {
        var i = 0
        val list = getInitialEmptyList(date)

        do {
            i++
            list.add(i.toString())
        } while(i < date.month.length(false))

        return list
    }

    fun getDates(date: LocalDate): ArrayList<String> {
        var i = 0
        var myDateId: String
        val list = getInitialEmptyList(date)

        do {
            i++
            myDateId = date.withDayOfMonth(i).toString()
            list.add(myDateId)
        } while (i < date.month.length(false))

        return list
    }

    private fun getInitialEmptyList(date: LocalDate): ArrayList<String> {
        val firstDayOfMonth = LocalDate.of(date.year, date.month, 1)
        val startOfWeek = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val daysDistance = ChronoUnit.DAYS.between(startOfWeek, firstDayOfMonth)

        return ArrayList(MutableList(daysDistance.toInt()) {""})
    }

    fun getPrettyDate(strDate: String?): String {
        val tokens = strDate!!.split(DATE_SEPARATOR)
        val date = LocalDate.of(tokens[0].toInt(), tokens[1].toInt(), tokens[2].toInt())

        return "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ITALIAN)}, ${tokens[2]} ${date.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN)}".lowercase()
    }
}