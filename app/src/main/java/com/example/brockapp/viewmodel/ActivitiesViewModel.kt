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

    private val _meters = MutableLiveData<Int>()
    val meters: LiveData<Int> get() = _meters

    private val _steps = MutableLiveData<Int>()
    val steps: LiveData<Int> get() = _steps

    fun getUserActivities(startOfDay: String, endOfDay: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val listActivities = ArrayList<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(User.id, startOfDay, endOfDay)
            val listVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(User.id, startOfDay, endOfDay)
            val listWalkingActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(User.id, startOfDay, endOfDay)

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

    fun getStillTime(startOfDay: String, endOfDay: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(
                User.id,
                startOfDay,
                endOfDay
            )

            val timeSpent = timeSpentCounter.computeTimeSpentStill(stillActivities)

            _stillTime.postValue(timeSpent)
        }
    }

    fun getKilometers(startOfDay: String, endOfDay: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val meters = db.UserVehicleActivityDao().getEndingVehicleActivitiesByUserIdAndPeriod(
                User.id,
                startOfDay,
                endOfDay
            ).parallelStream().mapToInt { it.distanceTravelled!!.toInt() }.sum()

            _meters.postValue(meters)
        }
    }

    fun getSteps(startOfDay: String, endOfDay: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val steps = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(
                User.id,
                startOfDay,
                endOfDay
            ).parallelStream().mapToInt { it.stepNumber.toInt() }.sum()

            _steps.postValue(steps)
        }
    }
}
