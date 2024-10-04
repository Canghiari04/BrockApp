package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.util.ArrayUtils.removeAll

class ActivitiesViewModel(private val db: BrockDB): ViewModel() {
    private val _listActivities = MutableLiveData<List<UserActivity>>()
    val listActivities: LiveData<List<UserActivity>> get() = _listActivities

    private val _mapCountActivities = MutableLiveData<Map<String, Int>>()
    val mapCountActivities: MutableLiveData<Map<String, Int>> get() = _mapCountActivities

    private val _stillTime = MutableLiveData<Long>()
    val stillTime: LiveData<Long> get() = _stillTime

    private val _listVehicleActivities = MutableLiveData<List<UserVehicleActivityEntity>>()
    val listVehicleActivities: LiveData<List<UserVehicleActivityEntity>> get() = _listVehicleActivities

    private val _meters = MutableLiveData<Double>()
    val meters: LiveData<Double> get() = _meters

    private val _vehicleTime = MutableLiveData<Long>()
    val vehicleTime: LiveData<Long> get() = _vehicleTime

    private val _listWalkActivities = MutableLiveData<List<UserWalkActivityEntity>>()
    val listWalkActivities: LiveData<List<UserWalkActivityEntity>> get() = _listWalkActivities

    private val _steps = MutableLiveData<Int>()
    val steps: LiveData<Int> get() = _steps

    private val _walkTime = MutableLiveData<Long>()
    val walkTime: LiveData<Long> get() = _walkTime

    // Get all the activities and put inside in an unique list
    fun getUserActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val listActivities = mutableListOf<UserActivity>()

            val listStillActivities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(
                MyUser.id, startOfPeriod, endOfPeriod)
            val listVehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(
                MyUser.id, startOfPeriod, endOfPeriod)
            val listWalkingActivities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(
                MyUser.id, startOfPeriod, endOfPeriod)

            listStillActivities.parallelStream().forEach {
                listActivities.add(
                    UserActivity(
                        it.id,
                        it.userId,
                        STILL_ACTIVITY_TYPE,
                        it.timestamp,
                        it.arrivalTime,
                        it.exitTime,
                        " "
                    )
                )
            }

            listVehicleActivities.parallelStream().forEach {
                listActivities.add(
                    UserActivity(
                        it.id,
                        it.userId,
                        VEHICLE_ACTIVITY_TYPE,
                        it.timestamp,
                        it.arrivalTime,
                        it.exitTime,
                        it.distanceTravelled
                    )
                )
            }

            listWalkingActivities.parallelStream().forEach {
                listActivities.add(
                    UserActivity(
                        it.id,
                        it.userId,
                        WALK_ACTIVITY_TYPE,
                        it.timestamp,
                        it.arrivalTime,
                        it.exitTime,
                        it.stepNumber
                    )
                )
            }

            listActivities.sortedBy { it.timestamp }
            listActivities.apply {
                removeAll { it.exitTime == 0L }
            }

            _listActivities.postValue(listActivities)
        }
    }

    // Return the number of activities done during a certain period
    fun getCountsOfActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stillActivitiesCount = db.UserStillActivityDao()
                .getStillActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList().apply { removeAll { it.exitTime == 0L } }

            val walkActivitiesCount = db.UserWalkActivityDao()
                .getWalkActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList().apply { removeAll { it.exitTime == 0L } }

            val vehicleActivitiesCount = db.UserVehicleActivityDao()
                .getVehicleActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList().apply { removeAll { it.exitTime == 0L } }

            val map = mutableMapOf(
                "Still" to stillActivitiesCount.size,
                "Vehicle" to vehicleActivitiesCount.size,
                "Walk" to walkActivitiesCount.size
            )

            _mapCountActivities.postValue(map)
        }
    }

    fun getStillTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            )

            val time = run {
                if (activities.isNotEmpty() && activities.size % 2 == 0) {
                    var sum = 0L

                    for (activity in activities) {
                        sum += (activity.exitTime - activity.arrivalTime)
                    }

                    sum
                } else {
                    0L
                }
            }

            _stillTime.postValue(time)
        }
    }

    fun getVehicleActivities(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod (
                MyUser.id,
                startOfWeek,
                endOfWeek
            )

            _listVehicleActivities.postValue(activities)
        }
    }

    fun getKilometers(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val meters = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            ).parallelStream().mapToDouble { it.distanceTravelled }.sum()

            _meters.postValue(meters)
        }
    }

    fun getVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            )

            val time = run {
                if (activities.isNotEmpty() && activities.size % 2 == 0) {
                    var sum = 0L

                    for (activity in activities) {
                        if (activity.exitTime > activity.arrivalTime) {
                            sum += (activity.exitTime - activity.arrivalTime)
                        }
                    }

                    sum
                } else {
                    0L
                }
            }

            _vehicleTime.postValue(time)
        }
    }

    fun getWalkActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod (
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            )

            _listWalkActivities.postValue(activities)
        }
    }

    fun getSteps(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val steps = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            ).parallelStream().mapToInt { it.stepNumber.toInt() }.sum()

            _steps.postValue(steps)
        }
    }

    fun getWalkTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            )

            val time = run {
                if (activities.isNotEmpty()) {
                    var sum = 0L

                    for (activity in activities) {
                        if (activity.exitTime > activity.arrivalTime) {
                            sum += (activity.exitTime - activity.arrivalTime)
                        }
                    }

                    sum
                } else {
                    0L
                }
            }

            _walkTime.postValue(time)
        }
    }
}