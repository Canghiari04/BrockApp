package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.FriendEntity
import com.example.brockapp.database.GeofenceTransitionEntity

import android.util.Log
import java.time.LocalDate
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import java.time.format.DateTimeFormatter
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.amazonaws.services.s3.AmazonS3Client
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarEntry
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.google.type.DateTime
import java.time.LocalDateTime

class GroupViewModel(private val s3Client: AmazonS3Client, private val db: BrockDB): ViewModel() {
    companion object {
        const val TO_KM = 1000f
        const val TO_MINUTES = 60000f
    }

    private val pattern = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

    private val _users = MutableLiveData<List<String>>()
    val users: LiveData<List<String>> get() = _users

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> get() = _suggestions

    private val _currentFriends = MutableLiveData<List<String>>()
    val currentFriends: LiveData<List<String>> get() = _currentFriends

    private val _friend = MutableLiveData<Friend>()
    val friend: LiveData<Friend> get() = _friend

    private val _friendMeters = MutableLiveData<Double>()
    val friendMeters: LiveData<Double> get() = _friendMeters

    private val _friendVehicleTime = MutableLiveData<Long>()
    val friendVehicleTime: LiveData<Long> get() = _friendVehicleTime

    private val _friendVehicleBarChartEntries = MutableLiveData<List<BarEntry>>()
    val friendVehicleBarChartEntries: LiveData<List<BarEntry>> get() = _friendVehicleBarChartEntries

    private val _friendStillTime = MutableLiveData<Long>()
    val friendStillTime: LiveData<Long> get() = _friendStillTime

    private val _friendStillBarChartEntries = MutableLiveData<List<BarEntry>>()
    val friendStillBarChartEntries: LiveData<List<BarEntry>> get() = _friendStillBarChartEntries

    private val _friendSteps = MutableLiveData<Int>()
    val friendSteps: LiveData<Int> get() = _friendSteps

    private val _friendWalkTime = MutableLiveData<Long>()
    val friendWalkTime: LiveData<Long> get() = _friendWalkTime

    private val _friendWalkBarChartEntries = MutableLiveData<List<BarEntry>>()
    val friendWalkBarChartEntries: LiveData<List<BarEntry>> get() = _friendWalkBarChartEntries

    private val _friendVehicleLineChartEntries = MutableLiveData<List<Entry>>()
    val friendVehicleLineChartEntries: LiveData<List<Entry>> get() = _friendVehicleLineChartEntries

    private val _friendWalkLineChartEntries = MutableLiveData<List<Entry>>()
    val friendWalkLineChartEntries: LiveData<List<Entry>> get() = _friendWalkLineChartEntries

    private val _friendPieChartEntries = MutableLiveData<List<PieEntry>>()
    val friendPieChartEntries: MutableLiveData<List<PieEntry>> get() = _friendPieChartEntries

    private val _errorAddFriend = MutableLiveData<Boolean>()
    val errorAddFriend: LiveData<Boolean> = _errorAddFriend

    private val _friendGeofenceTransitions = MutableLiveData<List<GeofenceTransitionEntity>>()
    val friendGeofenceTransitions: LiveData<List<GeofenceTransitionEntity>> = _friendGeofenceTransitions

    fun getAllUsers() {
        viewModelScope.launch(Dispatchers.Default) {
            val listObjectsRequest = ListObjectsRequest().withBucketName(BUCKET_NAME)

            val objectListing = s3Client.listObjects(listObjectsRequest)
            val s3Objects = objectListing.objectSummaries

            val matchingUsers = s3Objects
                .filter { it.key.endsWith(".json") && it.key != "user/${MyUser.username}.json" }
                .map { it.key.removePrefix("user/").removeSuffix(".json") }

            if (matchingUsers.isNotEmpty()) {
                _users.postValue(matchingUsers)
            }
        }
    }

    fun getSuggestions(user: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val userKey = "user/$user"
                val listObjectsRequest = ListObjectsRequest()
                    .withBucketName(BUCKET_NAME)
                    .withPrefix(userKey)

                val objectListing = s3Client.listObjects(listObjectsRequest)
                val s3Objects = objectListing.objectSummaries
                val matchingUsers = s3Objects
                    .filter {
                        it.key.endsWith(".json") && it.key != "user/${MyUser.username}.json"
                    }
                    .map {
                        it.key.removePrefix("user/").removeSuffix(".json")
                    }

                if(matchingUsers.isNotEmpty()) {
                    _suggestions.postValue(matchingUsers)
                } else {
                    _suggestions.postValue(listOf())
                }
            } catch (e: Exception) {
                Log.e("FRIENDS_VIEW_MODEL", e.toString())
            }
        }
    }

    // Function used to search if an user is currently a friend
    fun getCurrentFriends(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = db.FriendDao().getFriendsByUserId(id)
            _currentFriends.postValue(item)
        }
    }

    fun loadData(username: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            val friendKey = "user/$username.json"

            try {
                val request = GetObjectRequest(BUCKET_NAME, friendKey)
                val result = s3Client.getObject(request)
                val content = result.objectContent.bufferedReader().use { it.readText() }

                val gson = Gson()
                _friend.postValue(gson.fromJson(content, Friend::class.java))
            } catch (e: Exception) {
                Log.e("FRIENDS_VIEW_MODEL", e.toString())
            }
        }
    }

    fun getFriendVehicleTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _friendVehicleTime.postValue(time)
    }

    fun getFriendKilometers(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var meters = 0.0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if ((timeStamp.isAfter(start) && timeStamp.isBefore(end)) || timeStamp.isEqual(start)) {
                meters += it.distanceTravelled
            }
        }

        _friendMeters.postValue(meters)
    }

    fun getFriendVehicleBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val vehicleItems = friend.vehicleActivities

        val groupedItems = vehicleItems.groupBy {
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

        val entries = ArrayList<BarEntry>()

        for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
            val item = timePerDay[day]
            if (item != null) entries.add(BarEntry(day.toFloat(), item)) else entries.add(
                BarEntry(day.toFloat(), 0f)
            )
        }

        _friendVehicleBarChartEntries.postValue(entries)
    }

    fun getFriendStillTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.stillActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end) && it.exitTime > it.arrivalTime) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _friendStillTime.postValue(time)
    }

    fun getFriendStillBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val itemsStill = friend.stillActivities

        val groupedItems = itemsStill.groupBy {
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

        val entries = ArrayList<BarEntry>()

        for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
            val item = timePerDay[day]
            if (item != null) entries.add(BarEntry(day.toFloat(), item)) else entries.add(
                BarEntry(day.toFloat(), 0f)
            )
        }

        _friendStillBarChartEntries.postValue(entries)
    }

    fun getFriendSteps(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var steps = 0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.walkActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                steps += it.stepNumber.toInt()
            }
        }

        _friendSteps.postValue(steps)
    }

    fun getFriendWalkTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.walkActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end) && it.exitTime > it.arrivalTime) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _friendWalkTime.postValue(time)
    }

    fun getFriendWalkBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val itemsWalk = friend.walkActivities

        val groupedItems = itemsWalk.groupBy {
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

        val entries = ArrayList<BarEntry>()

        for (day in firstDay.dayOfMonth..lastDay.dayOfMonth) {
            val item = timePerDay[day]
            if (item != null) entries.add(BarEntry(day.toFloat(), item)) else entries.add(
                BarEntry(day.toFloat(), 0f)
            )
        }

        _friendWalkBarChartEntries.postValue(entries)
    }

    fun getFriendVehicleLineChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val groupedItems = friend.vehicleActivities.groupBy {
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

        _friendVehicleLineChartEntries.postValue(entries)
    }

    fun getFriendWalkLineChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val groupedItems = friend.walkActivities.groupBy {
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

        _friendWalkLineChartEntries.postValue(entries)
    }

    fun getFriendCountOfActivities(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        // I use scope function filter to avoid concurrent exceptions
        val vehicleActivities = friend.vehicleActivities
            .filter { it.exitTime > it.arrivalTime }
            .filter {
                val timeStamp = LocalDateTime.parse(it.timestamp, pattern)
                (timeStamp.isBefore(end) && timeStamp.isAfter(start))
            }

        val stillActivities = friend.stillActivities
            .filter { it.exitTime > it.arrivalTime }
            .filter {
                val timeStamp = LocalDateTime.parse(it.timestamp, pattern)
                (timeStamp.isBefore(end) && timeStamp.isAfter(start))
            }

        val walkActivities = friend.walkActivities
            .filter { it.exitTime > it.arrivalTime }
            .filter {
                val timeStamp = LocalDateTime.parse(it.timestamp, pattern)
                (timeStamp.isBefore(end) && timeStamp.isAfter(start))
            }

        val map = mutableMapOf(
            "Vehicle" to vehicleActivities.size,
            "Still" to stillActivities.size,
            "Walk" to walkActivities.size
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

        _friendPieChartEntries.postValue(entries)
    }

    fun addFriend(username: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (username != null) {
                val friend = FriendEntity(userId = MyUser.id, followedUsername = username)
                db.FriendDao().insertFriend(friend)

                _errorAddFriend.postValue(true)
            } else {
                _errorAddFriend.postValue(false)
            }
        }
    }

    fun getFriendGeofenceTransitions(friend: Friend) {
        val list = ArrayList<GeofenceTransitionEntity>()

        if (friend.geofenceTransitions.isNotEmpty()) {
            friend.geofenceTransitions.forEach {
                val transition = GeofenceTransitionEntity(
                    it.id,
                    it.userId,
                    it.nameLocation,
                    it.latitude,
                    it.longitude,
                    it.arrivalTime,
                    it.exitTime
                )

                list.add(transition)
            }
        }

        _friendGeofenceTransitions.postValue(list)
    }

    private fun getLocalDateFromTimeStamp(startOfPeriod: String, endOfPeriod: String): Pair<LocalDateTime, LocalDateTime> {
        val start = LocalDateTime.parse(startOfPeriod, pattern)
        val end = LocalDateTime.parse(endOfPeriod, pattern)

        return Pair(start, end)
    }
}