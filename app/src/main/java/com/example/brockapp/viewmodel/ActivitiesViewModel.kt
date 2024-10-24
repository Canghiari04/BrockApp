package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.UserRunActivityEntity
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

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

    private val _vehicleTime = MutableLiveData<Long>()
    val vehicleTime: LiveData<Long> get() = _vehicleTime

    private val _metersTravelled = MutableLiveData<Double>()
    val metersTravelled: LiveData<Double> get() = _metersTravelled

    private val _vehicleBarChartEntries = MutableLiveData<List<BarEntry>>()
    val vehicleBarChartEntries: LiveData<List<BarEntry>> get() = _vehicleBarChartEntries

    private val _runTime = MutableLiveData<Long>()
    val runTime: LiveData<Long> get() = _runTime

    private val _metersRun = MutableLiveData<Double>()
    val metersRun: LiveData<Double> get() = _metersRun

    private val _runBarChartEntries = MutableLiveData<List<BarEntry>>()
    val runBarChartEntries: LiveData<List<BarEntry>> get() = _runBarChartEntries

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

    private val _runLineChartEntries = MutableLiveData<List<Entry>>()
    val runLineChartEntries: LiveData<List<Entry>> get() = _runLineChartEntries

    private val _walkLineChartEntries = MutableLiveData<List<Entry>>()
    val walkLineChartEntries: LiveData<List<Entry>> get() = _walkLineChartEntries

    private val _pieChartEntries = MutableLiveData<List<PieEntry>>()
    val pieChartEntries: MutableLiveData<List<PieEntry>> get() = _pieChartEntries

    fun insertVehicleActivity(item: UserVehicleActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UserVehicleActivityDao().insertVehicleActivity(item)
        }
    }

    fun updateVehicleActivity(exitTime: Long, distanceTravelled: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UserVehicleActivityDao().getLastInsertedId()!!
            db.UserVehicleActivityDao().updateLastRecord(
                lastId,
                exitTime,
                distanceTravelled ?: 0.0
            )
        }
    }

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

            _metersTravelled.postValue(meters)
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

    fun insertRunActivity(item: UserRunActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UserRunActivityDao().insertRunActivity(item)
        }
    }

    fun updateRunActivity(exitTime: Long, distanceRun: Double?, heightDifference: Float?) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UserRunActivityDao().getLastInsertedId()!!
            db.UserRunActivityDao().updateLastRecord(
                lastId,
                exitTime,
                distanceRun ?: 0.0,
                heightDifference ?: 0f
            )
        }
    }

    fun getRunTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UserRunActivityDao().getRunActivitiesByUserIdAndPeriod(
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

            _runTime.postValue(time)
        }
    }

    fun getKilometersRun(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val meters = db.UserRunActivityDao().getRunActivitiesByUserIdAndPeriod(
                MyUser.id,
                startOfPeriod,
                endOfPeriod
            ).parallelStream().mapToDouble { it.distanceDone }.sum()

            _metersRun.postValue(meters)
        }
    }

    fun getRunBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserRunActivityDao().getRunActivitiesByUserIdAndPeriod(
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

            _runBarChartEntries.postValue(entries)
        }
    }

    fun insertStillActivity(item: UserStillActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UserStillActivityDao().insertStillActivity(item)
        }
    }

    fun updateStillActivity(exitTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UserStillActivityDao().getLastInsertedId()!!
            db.UserStillActivityDao().updateLastRecord(
                lastId,
                exitTime
            )
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

    fun insertWalkActivity(item: UserWalkActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UserWalkActivityDao().insertWalkActivity(item)
        }
    }

    fun updateWalkActivity(exitTime: Long, stepsNumber: Long?, heightDifference: Float?) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UserWalkActivityDao().getLastInsertedId()!!
            db.UserWalkActivityDao().updateLastRecord(
                lastId,
                exitTime,
                stepsNumber ?: 0L,
                heightDifference ?: 0f
            )
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
            ).parallelStream().mapToInt { it.stepsNumber.toInt() }.sum()

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

    fun getRunLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UserRunActivityDao().getRunActivitiesByUserIdAndPeriod (
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
                (it.value.sumOf { it.distanceDone } / TO_KM).toFloat()
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

            _runLineChartEntries.postValue(entries)
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
                (it.value.sumOf { it.stepsNumber }).toFloat()
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
                .toMutableList()

            val runActivitiesCount = db.UserRunActivityDao()
                .getRunActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList()

            val stillActivitiesCount = db.UserStillActivityDao()
                .getStillActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList()

            val walkActivitiesCount = db.UserWalkActivityDao()
                .getWalkActivitiesByUserIdAndPeriod(MyUser.id, startOfPeriod, endOfPeriod)
                .toMutableList()

            val map = mutableMapOf(
                "Vehicle" to vehicleActivitiesCount.size,
                "Run" to runActivitiesCount.size,
                "Still" to stillActivitiesCount.size,
                "Walk" to walkActivitiesCount.size
            )

            val entries = ArrayList<PieEntry>()

            map.forEach { (activityType, value) ->
                if (value > 0) {
                    val label = when (activityType) {
                        VEHICLE_ACTIVITY_TYPE -> "Vehicle"
                        RUN_ACTIVITY_TYPE -> "Run"
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