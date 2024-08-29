package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity

import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class ActivitiesViewModel(private val db: BrockDB): ViewModel() {
    private val _sortedDayExitActivitiesList = MutableLiveData<List<UserActivity>>()
    val sortedDayExitActivitiesList: LiveData<List<UserActivity>> get() = _sortedDayExitActivitiesList

    private val _sortedDayActivitiesList = MutableLiveData<List<UserActivity>>()
    val sortedDayActivitiesList: LiveData<List<UserActivity>> get() = _sortedDayActivitiesList

    private val _listActivities = MutableLiveData<List<UserActivity>>()
    val listActivities: LiveData<List<UserActivity>> get() = _listActivities

    private val _steps = MutableLiveData<Int>()
    val steps: LiveData<Int> get() = _steps

    fun getUserActivities(startOfDay: String, endOfDay: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val listActivities = ArrayList<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
            val listVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
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

            val listExitActivities = listActivities
                .filter {
                    it.transitionType == 1
                }
                .sortedBy {
                    it.timestamp
                }

            _listActivities.postValue(listExitActivities)
        }
    }

    fun getSteps(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val steps = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(
                user.id,
                LocalDate.now().atTime(0, 0, 0).toString(),
                LocalDate.now().atTime(23, 59, 59).toString()
            ).parallelStream().mapToInt { it.stepNumber.toInt() }.sum()

            _steps.postValue(steps)
        }
    }

    fun getDayUserActivities(date: String?, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDayRange(date)
            val listActivities = ArrayList<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
            val listVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
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

            _sortedDayActivitiesList.postValue(listActivities.sortedBy { it.timestamp })
            val listExitActivities = listActivities.filter { it.transitionType == 1 }
            _sortedDayExitActivitiesList.postValue(listExitActivities)
        }
    }

    private fun getDayRange(dateStr: String?): Pair<String, String> {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val startOfDay = date.atStartOfDay().withSecond(0)
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)

        val outputFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        return Pair(startOfDay.format(outputFormatter), endOfDay.format(outputFormatter))
    }
}
