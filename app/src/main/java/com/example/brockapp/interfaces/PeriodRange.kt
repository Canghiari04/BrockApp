package com.example.brockapp.interfaces

import java.time.LocalDate

interface PeriodRange {
    fun getDayRange(day: LocalDate): Pair<String, String>

    fun getWeekRange(day: LocalDate): Pair<String, String>

    fun getMonthRange(day: LocalDate): Pair<String, String>
}