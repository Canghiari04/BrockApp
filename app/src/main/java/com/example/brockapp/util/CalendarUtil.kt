package com.example.brockapp.util

import com.example.brockapp.DATE_SEPARATOR
import com.example.brockapp.ISO_DATE_FORMAT
import com.example.brockapp.data.UserActivity

import java.time.Month
import java.time.DayOfWeek
import java.time.Duration
import java.time.YearMonth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class CalendarUtil {
    fun getDateByTokens(formatter: DateTimeFormatter, tokens: List<String>): LocalDate {
        val year = tokens[1].toInt()
        val month = Month.valueOf(tokens[0].uppercase()).value
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

        return "${date.dayOfWeek}, ${tokens[2]} ${date.month}".lowercase()
    }

    fun computeTimeSpent(userActivities: List<UserActivity>): Long {
        var timeSpent = 0L
        val dateFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        for(i in userActivities.indices) {
            if(userActivities[i].transitionType == 1)
                continue

            val beginActivityTime = LocalDateTime.parse(userActivities[i].timestamp, dateFormatter)
            val nextActivity = if (i < userActivities.size - 1) userActivities[i + 1] else null

            if(nextActivity == null)
                break

            val endActivityTime = LocalDateTime.parse(nextActivity.timestamp, dateFormatter)

            val duration = Duration.between(beginActivityTime, endActivityTime)
            val durationInSeconds = duration.toMinutes() * 60

            timeSpent += durationInSeconds
        }

        return timeSpent
    }
}