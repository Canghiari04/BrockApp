package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser

import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import java.time.format.DateTimeFormatter
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry

class ActivitiesViewModel(private val db: BrockDB): ViewModel() {
    companion object {
        const val TO_KM = 1000f
        const val TO_MINUTES = 60000f
    }

    private val pattern = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

    private val _meters = MutableLiveData<Double>()
    val meters: LiveData<Double> get() = _meters

    private val _vehicleTime = MutableLiveData<Long>()
    val vehicleTime: LiveData<Long> get() = _vehicleTime

    private val _vehicleBarChartEntries = MutableLiveData<List<BarEntry>>()
    val vehicleBarChartEntries: LiveData<List<BarEntry>> get() = _vehicleBarChartEntries

    private val _stillTime = MutableLiveData<Long>()
    val stillTime: LiveData<Long> get() = _stillTime

    private val _stillBarChartEntries = MutableLiveData<List<BarEntry>>()
    val stillBarChartEntries: LiveData<List<BarEntry>> get() = _stillBarChartEntries

    private val _steps = MutableLiveData<Int>()
    val steps: LiveData<Int> get() = _steps

    private val _walkTime = MutableLiveData<Long>()
    val walkTime: LiveData<Long> get() = _walkTime

    private val _walkBarChartEntries = MutableLiveData<List<BarEntry>>()
    val walkBarChartEntries: LiveData<List<BarEntry>> get() = _walkBarChartEntries

    private val _vehicleLineChartEntries = MutableLiveData<List<Entry>>()
    val vehicleLineChartEntries: LiveData<List<Entry>> get() = _vehicleLineChartEntries

    private val _walkLineChartEntries = MutableLiveData<List<Entry>>()
    val walkLineChartEntries: LiveData<List<Entry>> get() = _walkLineChartEntries

    private val _pieChartEntries = MutableLiveData<List<PieEntry>>()
    val pieChartEntries: MutableLiveData<List<PieEntry>> get() = _pieChartEntries

    fun getVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            )

            // Whole time spent during the week
            val time = run {
                var sum = 0L

                for (activity in activities) {
                    if (activity.exitTime > activity.arrivalTime) {
                        sum += (activity.exitTime - activity.arrivalTime)
                    }
                }

                sum
            }

            _vehicleTime.postValue(time)
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


    fun getVehicleBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfWeek,
                endOfWeek
            )

            // All the activities are grouped by the day
            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            // Define the time for each week's day
            val timePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.exitTime - it.arrivalTime } / TO_MINUTES)
            }

            val entries = ArrayList<BarEntry>()

            for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
                val item = timePerDay[day]
                if (item != null) entries.add(BarEntry(day.toFloat(), item)) else entries.add(BarEntry(day.toFloat(), 0f))
            }

            _vehicleBarChartEntries.postValue(entries)
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
                if (activities.isNotEmpty()) {
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

    fun getStillBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserStillActivityDao().getStillActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfWeek,
                endOfWeek
            )

            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            val timePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.exitTime - it.arrivalTime } / TO_MINUTES).toFloat()
            }

            val entries = ArrayList<BarEntry>()

            for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
                val item = timePerDay[day]

                if (item != null) entries.add(BarEntry(day.toFloat(), item)) else entries.add(BarEntry(day.toFloat(), 0f))
            }

            _stillBarChartEntries.postValue(entries)
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

    fun getWalkBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfWeek,
                endOfWeek
            )

            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            val timePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.exitTime - it.arrivalTime } / TO_MINUTES).toFloat()
            }

            val entries = ArrayList<BarEntry>()

            for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
                val item = timePerDay[day]

                if (item != null) entries.add(BarEntry(day.toFloat(), item)) else entries.add(BarEntry(day.toFloat(), 0f))
            }

            _walkBarChartEntries.postValue(entries)
        }
    }

    fun getVehicleLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserVehicleActivityDao().getVehicleActivitiesByUserIdAndPeriod (
                MyUser.id,
                startOfWeek,
                endOfWeek
            )

            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            val distancePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.distanceTravelled } / TO_KM).toFloat()
            }

            val entries = ArrayList<Entry>()

            for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
                val item = distancePerDay[day]

                if (item != null) {
                    entries.add(Entry(day.toFloat(), item))
                } else {
                    entries.add(Entry(day.toFloat(), 0f))
                }
            }

            _vehicleLineChartEntries.postValue(entries)
        }
    }

    fun getWalkLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserWalkActivityDao().getWalkActivitiesByUserIdAndPeriod (
                MyUser.id,
                startOfWeek,
                endOfWeek
            )

            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            val stepsPerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.stepNumber }).toFloat()
            }

            val entries = ArrayList<Entry>()

            for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
                val item = stepsPerDay[day]

                if (item != null) {
                    entries.add(Entry(day.toFloat(), item))
                } else {
                    entries.add(Entry(day.toFloat(), 0f))
                }
            }

            _walkLineChartEntries.postValue(entries)
        }
    }

    fun getCountOfActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val vehicleActivitiesCount = db.UserVehicleActivityDao()
                .getVehicleActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList().apply { removeAll { it.exitTime == 0L } }

            val stillActivitiesCount = db.UserStillActivityDao()
                .getStillActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList().apply { removeAll { it.exitTime == 0L } }

            val walkActivitiesCount = db.UserWalkActivityDao()
                .getWalkActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList().apply { removeAll { it.exitTime == 0L } }

            val map = mutableMapOf(
                "Vehicle" to vehicleActivitiesCount.size,
                "Still" to stillActivitiesCount.size,
                "Walk" to walkActivitiesCount.size
            )

            val entries = ArrayList<PieEntry>()

            map.forEach { (activityType, value) ->
                if (value > 0) {
                    val label = when (activityType) {
                        VEHICLE_ACTIVITY_TYPE -> "Vehicle"
                        STILL_ACTIVITY_TYPE -> "Still"
                        WALK_ACTIVITY_TYPE -> "Walk"
                        else -> "Unknown"
                    }

                    entries.add(PieEntry(value.toFloat(), label))
                }
            }

            _pieChartEntries.postValue(entries)
        }
    }
}