package com.example.brockapp.interfaces

import com.example.brockapp.*

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class PeriodRangeImpl: PeriodRange {

    val datesOfWeek = mutableMapOf<Int, String>().apply {
        val range = getWeekRange(LocalDate.now())

        val firstDay = LocalDate.parse(range.first, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT))
        val lastDay = LocalDate.parse(range.second, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT))

        var i = 1
        var currentDay = firstDay
        while (!currentDay.isAfter(lastDay)) {
            val month = currentDay.month.toString().lowercase().capitalize().take(3)
            this[i] = "$month ${currentDay.dayOfMonth}, ${currentDay.year}"
            currentDay = currentDay.plusDays(1)
            i++
        }
    }

    override fun getDayRange(day: LocalDate): Pair<String, String> {
        val startOfDay = day.atStartOfDay().withSecond(0)
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }

    override fun getWeekRange(day: LocalDate): Pair<String, String> {
        val firstDay = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay().withSecond(0)
        val lastDay = day.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atStartOfDay().plusDays(1).minusSeconds(1)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(firstDay.format(outputFormatter), lastDay.format(outputFormatter))
    }

    override fun getMonthRange(day: LocalDate): Pair<String, String> {
        val firstDay = day.withDayOfMonth(1).atStartOfDay()
        val lastDay = day.withDayOfMonth(day.lengthOfMonth()).atTime(23, 59, 59)
        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(firstDay.format(outputFormatter), lastDay.format(outputFormatter))
    }
}