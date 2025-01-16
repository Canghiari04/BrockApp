package com.example.brockapp.viewModel

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.data.Activity
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.room.UsersRunActivityEntity
import com.example.brockapp.room.UsersWalkActivityEntity
import com.example.brockapp.room.UsersStillActivityEntity
import com.example.brockapp.room.UsersVehicleActivityEntity

import java.time.LocalDate
import java.time.LocalDateTime
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
import kotlin.time.Duration.Companion.milliseconds

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

    private val _listActivities = MutableLiveData<List<Activity>>()
    val listActivities: LiveData<List<Activity>> get() = _listActivities

    fun insertVehicleActivity(item: UsersVehicleActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UsersVehicleActivityDao().insertVehicleActivity(item)
        }
    }

    fun updateVehicleActivity(exitTime: Long, distanceTravelled: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UsersVehicleActivityDao().getLastInsertedId()

            db.UsersVehicleActivityDao().updateLastRecord(
                lastId,
                exitTime,
                distanceTravelled ?: 0.0
            )
        }
    }

    fun getVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UsersVehicleActivityDao().getVehicleActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfPeriod,
                endOfPeriod
            ).filter { it.distanceTravelled > 0.0 }

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
            val meters = db.UsersVehicleActivityDao().getVehicleActivitiesByUsernameAndPeriod(
                MyUser.username,
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

            val items = db.UsersVehicleActivityDao().getVehicleActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfWeek,
                endOfWeek
            ).filter { it.distanceTravelled > 0.0 }

            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            val timePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.exitTime - it.arrivalTime } / TO_MINUTES)
            }

            val entries = defineBarChartEntries(firstDay, lastDay, timePerDay)

            _vehicleBarChartEntries.postValue(entries)
        }
    }

    fun insertRunActivity(item: UsersRunActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UsersRunActivityDao().insertRunActivity(item)
        }
    }

    fun updateRunActivity(exitTime: Long, distanceRun: Double?, heightDifference: Float?) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UsersRunActivityDao().getLastInsertedId()

            db.UsersRunActivityDao().updateLastRecord(
                lastId,
                exitTime,
                distanceRun ?: 0.0,
                heightDifference ?: 0f
            )
        }
    }

    fun getRunTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UsersRunActivityDao().getRunActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfPeriod,
                endOfPeriod
            ).filter { it.distanceDone > 0.0 }

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
            val meters = db.UsersRunActivityDao().getRunActivitiesByUsernameAndPeriod(
                MyUser.username,
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

            val items = db.UsersRunActivityDao().getRunActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfWeek,
                endOfWeek
            ).filter { it.distanceDone > 0.0 || it.heightDifference > 0f }

            val groupedItems = items.groupBy {
                it.timestamp.let { timestamp ->
                    LocalDate.parse(
                        timestamp,
                        pattern
                    ).dayOfMonth
                }
            }

            val timePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.exitTime - it.arrivalTime } / TO_MINUTES)
            }

            val entries = defineBarChartEntries(firstDay, lastDay, timePerDay)

            _runBarChartEntries.postValue(entries)
        }
    }

    fun insertStillActivity(item: UsersStillActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UsersStillActivityDao().insertStillActivity(item)
        }
    }

    fun updateStillActivity(exitTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UsersStillActivityDao().getLastInsertedId()

            db.UsersStillActivityDao().updateLastRecord(
                lastId,
                exitTime
            )
        }
    }

    fun getStillTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UsersStillActivityDao().getStillActivitiesByUsernameAndPeriod(
                MyUser.username,
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

            val items = db.UsersStillActivityDao().getStillActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfWeek,
                endOfWeek
            )

            val groupedItems = items.groupBy {
                LocalDate.parse(
                    it.timestamp,
                    pattern
                ).dayOfMonth
            }

            val timePerDay = groupedItems.mapValues { it ->
                (it.value.sumOf { it.exitTime - it.arrivalTime } / TO_MINUTES)
            }

            val entries = defineBarChartEntries(firstDay, lastDay, timePerDay)

            _stillBarChartEntries.postValue(entries)
        }
    }

    fun insertWalkActivity(item: UsersWalkActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UsersWalkActivityDao().insertWalkActivity(item)
        }
    }

    fun updateWalkActivity(exitTime: Long, stepsNumber: Long?, heightDifference: Float?) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastId = db.UsersWalkActivityDao().getLastInsertedId()

            db.UsersWalkActivityDao().updateLastRecord(
                lastId,
                exitTime,
                stepsNumber ?: 0L,
                heightDifference ?: 0f
            )
        }
    }

    fun getWalkTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activities = db.UsersWalkActivityDao().getWalkActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfPeriod,
                endOfPeriod
            ).filter { it.stepsNumber > 0L }

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
            val steps = db.UsersWalkActivityDao().getWalkActivitiesByUsernameAndPeriod(
                MyUser.username,
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

            val items = db.UsersWalkActivityDao().getWalkActivitiesByUsernameAndPeriod(
                MyUser.username,
                startOfWeek,
                endOfWeek
            ).filter { it.stepsNumber > 0 || it.heightDifference > 0f }

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

            val entries = defineBarChartEntries(firstDay, lastDay, timePerDay)

            _walkBarChartEntries.postValue(entries)
        }
    }

    fun getVehicleLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UsersVehicleActivityDao().getVehicleActivitiesByUsernameAndPeriod (
                MyUser.username,
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

            val entries = defineLineChartEntries(firstDay, lastDay.plusDays(1), distancePerDay)

            _vehicleLineChartEntries.postValue(entries)
        }
    }

    fun getRunLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UsersRunActivityDao().getRunActivitiesByUsernameAndPeriod(
                MyUser.username,
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

            val entries = defineLineChartEntries(firstDay, lastDay.plusDays(1), distancePerDay)

            _runLineChartEntries.postValue(entries)
        }
    }

    fun getWalkLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val firstDay = LocalDate.parse(startOfWeek, pattern)
            val lastDay = LocalDate.parse(endOfWeek, pattern)

            val items = db.UsersWalkActivityDao().getWalkActivitiesByUsernameAndPeriod (
                MyUser.username,
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

            val entries = defineLineChartEntries(firstDay, lastDay.plusDays(1), stepsPerDay)

            _walkLineChartEntries.postValue(entries)
        }
    }

    fun getCountOfActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val vehicleActivities = db.UsersVehicleActivityDao()
                .getVehicleActivitiesByUsernameAndPeriod(MyUser.username, startOfPeriod, endOfPeriod)
                .filter { it.distanceTravelled > 0.0 }
                .toMutableList()

            val runActivities = db.UsersRunActivityDao()
                .getRunActivitiesByUsernameAndPeriod(MyUser.username, startOfPeriod, endOfPeriod)
                .filter { it.distanceDone > 0.0 }
                .toMutableList()

            val stillActivities = db.UsersStillActivityDao()
                .getStillActivitiesByUsernameAndPeriod(MyUser.username, startOfPeriod, endOfPeriod)
                .filter { it.exitTime > it.arrivalTime }
                .toMutableList()

            val walkActivities = db.UsersWalkActivityDao()
                .getWalkActivitiesByUsernameAndPeriod(MyUser.username, startOfPeriod, endOfPeriod)
                .filter { it.stepsNumber > 0L }
                .toMutableList()

            val map = mutableMapOf(
                "Vehicle" to vehicleActivities.size,
                "Run" to runActivities.size,
                "Still" to stillActivities.size,
                "Walk" to walkActivities.size
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

    fun getAllActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = listOf(
                    db.UsersVehicleActivityDao().getVehicleActivitiesByUsernameAndPeriod(
                        MyUser.username,
                        startOfPeriod,
                        endOfPeriod
                    ).map { activity ->
                        extractActivity("Vehicle activity", { activity.id }, { activity.timestamp }, { activity.arrivalTime }, { activity.exitTime }, activity)
                    },

                    db.UsersRunActivityDao().getRunActivitiesByUsernameAndPeriod(
                        MyUser.username,
                        startOfPeriod,
                        endOfPeriod
                    ).map { activity ->
                        extractActivity("Run activity", { activity.id }, { activity.timestamp }, { activity.arrivalTime }, { activity.exitTime }, activity)
                    },

                    db.UsersStillActivityDao().getStillActivitiesByUsernameAndPeriod(
                        MyUser.username,
                        startOfPeriod,
                        endOfPeriod
                    ).map { activity ->
                        extractActivity("Still activity", { activity.id }, { activity.timestamp }, { activity.arrivalTime }, { activity.exitTime }, activity)
                    },

                    db.UsersWalkActivityDao().getWalkActivitiesByUsernameAndPeriod(
                        MyUser.username,
                        startOfPeriod,
                        endOfPeriod
                    ).map { activity ->
                        extractActivity("Walk activity", { activity.id }, { activity.timestamp }, { activity.arrivalTime }, { activity.exitTime }, activity)
                    }
            ).flatten().sortedBy { activity ->
                LocalDateTime.parse(
                    activity.timestamp,
                    DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)
                )
            }

            _listActivities.postValue(list)
        }
    }

    fun deleteActivity(id: Long, type: String) {
        viewModelScope.launch {
            when {
                type.contains("Vehicle") -> {
                    db.UsersVehicleActivityDao().deleteVehicleActivity(id)
                }

                type.contains("Run") -> {
                    db.UsersRunActivityDao().deleteRunActivity(id)
                }

                type.contains("Still") -> {
                    db.UsersStillActivityDao().deleteStillActivity(id)
                }

                type.contains("Walk") -> {
                    db.UsersWalkActivityDao().deleteWalkActivity(id)
                }
            }
        }
    }

    private fun defineBarChartEntries(firstDay: LocalDate, lastDay: LocalDate, timePerDay: Map<Int, Float>): List<BarEntry> {
        val entries = ArrayList<BarEntry>()

        var i = 0
        var currentDay = firstDay
        while (!currentDay.isAfter(lastDay)) {
            val day = currentDay.dayOfMonth
            val item = timePerDay[day]

            currentDay = currentDay.plusDays(1)
            i++

            if (item != null) {
                entries.add(BarEntry(i.toFloat(), item))
            } else {
                entries.add(BarEntry(i.toFloat(), 0f))
            }
        }

        return entries
    }

    private fun defineLineChartEntries(firstDay: LocalDate, lastDay: LocalDate, items: Map<Int, Float>):  List<Entry> {
        val entries = ArrayList<Entry>()

        var i = 0
        var currentDay = firstDay
        while (!currentDay.isAfter(lastDay)) {
            val day = currentDay.dayOfMonth
            val item = items[day]

            currentDay = currentDay.plusDays(1)
            i++

            if (item != null) {
                entries.add(Entry(i.toFloat(), item))
            } else {
                entries.add(Entry(i.toFloat(), 0f))
            }
        }

        return entries
    }

    private fun <T> extractActivity(type: String, id: (T) -> Long, timestamp: (T) -> String, arrivalTime: (T) -> Long, exitTime: (T) -> Long, raw: T): Activity {
        return Activity(
            id = id(raw),
            type = type,
            timestamp = timestamp(raw),
            duration = "Time spent ${defineDuration(arrivalTime(raw), exitTime(raw))}"
        )
    }

    private fun defineDuration(arrivalTime: Long, exitTime: Long): String {
        val timeSpent = exitTime - arrivalTime

        return timeSpent.milliseconds.toComponents { hours, minutes, seconds, _ ->
            "%01dh %01dm %01ds".format(hours, minutes, seconds)
        }
    }
}