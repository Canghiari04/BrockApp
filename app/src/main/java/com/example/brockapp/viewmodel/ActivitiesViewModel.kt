package com.example.brockapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brockapp.STILL_ACTIVITY_TYPE
import com.example.brockapp.VEHICLE_ACTIVITY_TYPE
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.data.UserActivity
import com.example.brockapp.database.BrockDB
import com.example.brockapp.interfaces.TimeSpentCounterImpl
import com.example.brockapp.singleton.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivitiesViewModel(private val db: BrockDB): ViewModel() {
    private val timeSpentCounter = TimeSpentCounterImpl()

    private val _listExitActivities = MutableLiveData<List<UserActivity>>()
    val listExitActivities: LiveData<List<UserActivity>> get() = _listExitActivities

    private val _listTimeStampActivities = MutableLiveData<List<UserActivity>>()
    val listTimeStampActivities: LiveData<List<UserActivity>> get() = _listTimeStampActivities

    private val _stillTime = MutableLiveData<Long>()
    val stillTime: LiveData<Long> get() = _stillTime

    private val _kilometers = MutableLiveData<Int>()
    val kilometers: LiveData<Int> get() = _kilometers

    private val _steps = MutableLiveData<Int>()
    val steps: LiveData<Int> get() = _steps

    fun getUserActivities(startOfDay: String, endOfDay: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val listActivities = ArrayList<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
            val listVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)
            val listWalkingActivities = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(user.id, startOfDay, endOfDay)

            listStillActivities.parallelStream().forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, STILL_ACTIVITY_TYPE, "")
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

            val listTimeStampActivities = listActivities
                .sortedBy {
                    it.timestamp
                }

            val listExitActivities = listActivities
                .filter {
                    it.transitionType == 1
                }
                .sortedBy {
                    it.timestamp
                }

            _listTimeStampActivities.postValue(listTimeStampActivities)
            _listExitActivities.postValue(listExitActivities)
        }
    }

    fun getStillTime(startOfDay: String, endOfDay: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val stillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(
                user.id,
                startOfDay,
                endOfDay
            )

            val timeSpent = timeSpentCounter.computeTimeSpentStill(stillActivities)

            _stillTime.postValue(timeSpent)
        }
    }

    fun getKilometers(startOfDay: String, endOfDay: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val kilometers = db.UserVehicleActivityDao().getEndingVehicleActivitiesByUserIdAndPeriod(
                user.id,
                startOfDay,
                endOfDay
            ).parallelStream().mapToInt {it.distanceTravelled!!.toInt()}.sum()

            _kilometers.postValue(kilometers)
        }
    }

    fun getSteps(startOfDay: String, endOfDay: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val steps = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(
                user.id,
                startOfDay,
                endOfDay
            ).parallelStream().mapToInt { it.stepNumber.toInt() }.sum()

            _steps.postValue(steps)
        }
    }
}
