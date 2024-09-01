package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class ChartsViewModel(private val db: BrockDB): ViewModel() {
    private val _vehicleActivities = MutableLiveData<List<UserVehicleActivityEntity>>()
    val vehicleActivities: LiveData<List<UserVehicleActivityEntity>> get() = _vehicleActivities

    private val _walkActivities = MutableLiveData<List<UserWalkActivityEntity>>()
    val walkActivities: LiveData<List<UserWalkActivityEntity>> get() = _walkActivities

    private val _mapCountActivities = MutableLiveData<Map<String, Int>>()
    val mapCountActivities: MutableLiveData<Map<String, Int>> get() = _mapCountActivities

    fun getChartsVehicleActivities(date: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))
            val currentDate = yearMonth.atDay(1)
            val startOfMonth = currentDate.atStartOfDay()
            val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).atTime(LocalTime.MAX)

            val listWalkActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(
                user.id,
                startOfMonth.toString(),
                endOfMonth.toString()
            )

            _walkActivities.postValue(listWalkActivities)
        }
    }

    fun getChartsWalkActivities(date: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))
            val currentDate = yearMonth.atDay(1)
            val startOfMonth = currentDate.atStartOfDay()
            val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).atTime(LocalTime.MAX)

            val listVehicleActivity = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(
                user.id,
                startOfMonth.toString(),
                endOfMonth.toString()
            )

            _vehicleActivities.postValue(listVehicleActivity)
        }
    }

    fun getCountsOfActivities(date: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern(CHARTS_DATE_FORMAT))
            val currentDate = yearMonth.atDay(1)
            val (startOfMonth, endOfMonth) = getMonthRange(currentDate)

            val walkActivitiesCount = db.UserWalkActivityDao().getWalkActivitiesCountByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
            val stillActivitiesCount = db.UserStillActivityDao().getStillActivitiesCountByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)
            val vehicleActivitiesCount = db.UserVehicleActivityDao().getVehicleActivitiesCountByUserIdAndPeriod(user.id, startOfMonth, endOfMonth)

            val map = mapOf(
                "STILL" to stillActivitiesCount,
                "VEHICLE" to vehicleActivitiesCount,
                "WALK" to walkActivitiesCount
            )

            _mapCountActivities.postValue(map)
        }
    }

    private fun getMonthRange(date: LocalDate): Pair<String, String> {
        val startOfMonth = date.withDayOfMonth(1).atStartOfDay()
        val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(
            startOfMonth.format(outputFormatter),
            endOfMonth.format(outputFormatter)
        )
    }
}