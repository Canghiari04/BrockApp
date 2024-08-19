package com.example.brockapp.viewmodel

import com.example.brockapp.singleton.User
import com.example.brockapp.ISO_DATE_FORMAT
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE

import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.format.DateTimeFormatter
import androidx.lifecycle.MutableLiveData
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

class ActivitiesViewModel(private val db: BrockDB): ViewModel() {
    private val _sortedDayActivitiesList = MutableLiveData<List<UserActivity>>()
    val sortedDayActivitiesList: LiveData<List<UserActivity>> get() = _sortedDayActivitiesList

    private val _sortedWeekActivitiesList = MutableLiveData<List<UserActivity>>()
    val sortedWeekActivitiesDayList: LiveData<List<UserActivity>> get() = _sortedWeekActivitiesList

    fun getDayUserActivities(date: String?, user: User) {
        viewModelScope.launch {
            val (startOfDay, endOfDay) = getDayRange(date)
            val listActivities = ArrayList<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getEndingStillActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
            val listVehicleActivities = db.UserVehicleActivityDao().getEndingVehicleActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
            val listWalkingActivities = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)

            listStillActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, STILL_ACTIVITY_TYPE, "METTERE DURATA STILL")
                listActivities.add(newActivity)
            }

            listVehicleActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, VEHICLE_ACTIVITY_TYPE, it.distanceTravelled.toString())
                listActivities.add(newActivity)
            }

            listWalkingActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, WALK_ACTIVITY_TYPE, it.stepNumber.toString())
                listActivities.add(newActivity)
            }

            _sortedDayActivitiesList.value = listActivities.sortedBy { it.timestamp }
        }
    }

    fun getWeekUserActivities(day: LocalDate, user: User) {
        viewModelScope.launch {
            val (startOfWeek, endOfWeek) = getWeekRange(day)
            val listActivities = mutableListOf<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getEndingStillActivitiesByUserIdAndPeriod(user.id, startOfWeek, endOfWeek)
            val listVehicleActivities = db.UserVehicleActivityDao().getEndingVehicleActivitiesByUserIdAndPeriod(user.id, startOfWeek, endOfWeek)
            val listWalkingActivities = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(user.id, startOfWeek, endOfWeek)

            listStillActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, STILL_ACTIVITY_TYPE, "METTERE DURATA STILL")
                listActivities.add(newActivity)
            }

            listVehicleActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, VEHICLE_ACTIVITY_TYPE, it.distanceTravelled.toString())
                listActivities.add(newActivity)
            }

            listWalkingActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, WALK_ACTIVITY_TYPE, it.stepNumber.toString())
                listActivities.add(newActivity)
            }

            _sortedWeekActivitiesList.value = listActivities.sortedBy { it.timestamp }
        }
    }

    private fun getDayRange(dateStr: String?): Pair<String, String> {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val startOfDay = date.atStartOfDay().withSecond(0)
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }

    private fun getWeekRange(currentDay: LocalDate): Pair<String, String> {
        val firstDay = currentDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay().withSecond(0)
        val lastDay = currentDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atStartOfDay().plusDays(1).minusSeconds(1)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(firstDay.format(outputFormatter), lastDay.format(outputFormatter))
    }
}