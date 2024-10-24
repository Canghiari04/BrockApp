package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.data.User
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.FriendEntity
import com.example.brockapp.database.GeofenceTransitionEntity

import android.util.Log
import java.time.LocalDate
import com.google.gson.Gson
import java.time.LocalDateTime
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

class GroupViewModel(private val s3Client: AmazonS3Client, private val db: BrockDB): ViewModel() {
    companion object {
        const val TO_KM = 1000f
        const val TO_MINUTES = 60000f
    }

    private val pattern = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

    private val _subscribers = MutableLiveData<List<User?>>()
    val subscribers: LiveData<List<User?>> get() = _subscribers

    private val _friends = MutableLiveData<List<User?>>()
    val friends: LiveData<List<User?>> get() = _friends

    private val _suggestions = MutableLiveData<List<User?>>()
    val suggestions: LiveData<List<User?>> get() = _suggestions

    private val _currentFriends = MutableLiveData<List<String>>()
    val currentFriends: LiveData<List<String>> get() = _currentFriends

    private val _friend = MutableLiveData<Friend>()
    val friend: LiveData<Friend> get() = _friend

    private val _userVehicleTime = MutableLiveData<Long>()
    val userVehicleTime: LiveData<Long> get() = _userVehicleTime

    private val _userMetersTravelled = MutableLiveData<Double>()
    val userMetersTravelled: LiveData<Double> get() = _userMetersTravelled

    private val _userVehicleBarChartEntries = MutableLiveData<List<BarEntry>>()
    val userVehicleBarChartEntries: LiveData<List<BarEntry>> get() = _userVehicleBarChartEntries

    private val _userRunTime = MutableLiveData<Long>()
    val userRunTime: LiveData<Long> get() = _userRunTime

    private val _userMetersRun = MutableLiveData<Double>()
    val userMetersRun: LiveData<Double> get() = _userMetersRun

    private val _userRunBarChartEntries = MutableLiveData<List<BarEntry>>()
    val userRunBarChartEntries: LiveData<List<BarEntry>> get() = _userRunBarChartEntries

    private val _userStillTime = MutableLiveData<Long>()
    val userStillTime: LiveData<Long> get() = _userStillTime

    private val _userStillBarChartEntries = MutableLiveData<List<BarEntry>>()
    val userStillBarChartEntries: LiveData<List<BarEntry>> get() = _userStillBarChartEntries

    private val _userWalkTime = MutableLiveData<Long>()
    val userWalkTime: LiveData<Long> get() = _userWalkTime

    private val _userSteps = MutableLiveData<Int>()
    val userSteps: LiveData<Int> get() = _userSteps

    private val _userWalkBarChartEntries = MutableLiveData<List<BarEntry>>()
    val userWalkBarChartEntries: LiveData<List<BarEntry>> get() = _userWalkBarChartEntries

    private val _userVehicleLineChartEntries = MutableLiveData<List<Entry>>()
    val userVehicleLineChartEntries: LiveData<List<Entry>> get() = _userVehicleLineChartEntries

    private val _userRunLineChartEntries = MutableLiveData<List<Entry>>()
    val userRunLineChartEntries: LiveData<List<Entry>> get() = _userRunLineChartEntries

    private val _userWalkLineChartEntries = MutableLiveData<List<Entry>>()
    val userWalkLineChartEntries: LiveData<List<Entry>> get() = _userWalkLineChartEntries

    private val _userPieChartEntries = MutableLiveData<List<PieEntry>>()
    val userPieChartEntries: MutableLiveData<List<PieEntry>> get() = _userPieChartEntries

    private val _userGeofenceTransitions = MutableLiveData<List<GeofenceTransitionEntity>>()
    val userGeofenceTransitions: LiveData<List<GeofenceTransitionEntity>> = _userGeofenceTransitions

    private val _errorAddFriend = MutableLiveData<Boolean>()
    val errorAddFriend: LiveData<Boolean> = _errorAddFriend

    private val _errorDeleteFriend = MutableLiveData<Boolean>()
    val errorDeleteFriend: LiveData<Boolean> = _errorDeleteFriend

    // This function must be called while the app is starting
    fun getAllSubscribers() {
        viewModelScope.launch(Dispatchers.IO) {
            val listObjectsRequest = ListObjectsRequest().withBucketName(BuildConfig.BUCKET_NAME)

            val objectListing = s3Client.listObjects(listObjectsRequest)
            val s3Objects = objectListing.objectSummaries

            val items = s3Objects
                .filter { it.key.endsWith(".json") && it.key != "user/${MyUser.username}.json" }
                .map { it.key.removePrefix("user/").removeSuffix(".json") }

            val subscribers = mutableListOf<User?>()
            val friends = db.FriendDao().getAllFriends()

            // Items contains all the usernames
            if (items.isNotEmpty()) {
                for (item in items) {
                    if (!friends.contains(item)) subscribers.add(loadDataUser(item))
                }
            }

            _subscribers.postValue(subscribers)
        }
    }

    fun getAllFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            // Define all the username to search inside the Bucket
            val listUsernames = mutableListOf<String>().also {
                val usernames = db.FriendDao().getAllFriends()

                for (username in usernames) {
                    it.add("user/$username.json")
                }
            }

            val listObjectsRequest = ListObjectsRequest().withBucketName(BuildConfig.BUCKET_NAME)

            val objectListing = s3Client.listObjects(listObjectsRequest)
            val s3Objects = objectListing.objectSummaries

            val items = s3Objects
                .filter { listUsernames.contains(it.key) }
                .filter { it.key.endsWith(".json") }
                .filter { it.key != "user/${MyUser.username}.json" }
                .map { it.key.removePrefix("user/").removeSuffix(".json") }

            val friends = mutableListOf<User?>()

            if (items.isNotEmpty()) {
                for (item in items) {
                    friends.add(loadDataUser(item))
                }
            }

            _friends.postValue(friends)
        }
    }

    fun getSuggestions(user: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val userKey = "user/$user"
                val listObjectsRequest = ListObjectsRequest()
                    .withBucketName(BuildConfig.BUCKET_NAME)
                    .withPrefix(userKey)

                val objectListing = s3Client.listObjects(listObjectsRequest)
                val s3Objects = objectListing.objectSummaries
                val matchingSubscribers = s3Objects
                    .filter {
                        it.key.endsWith(".json") && it.key != "user/${MyUser.username}.json"
                    }
                    .map {
                        it.key.removePrefix("user/").removeSuffix(".json")
                    }

                if(matchingSubscribers.isNotEmpty()) {
                    val subscribers = mutableListOf<User?>()

                    for (match in matchingSubscribers) {
                        subscribers.add(loadDataUser(match))
                    }

                    _suggestions.postValue(subscribers)
                } else {
                    _suggestions.postValue(listOf())
                }
            } catch (e: Exception) {
                Log.e("GROUP_VIEW_MODEL", e.toString())
            }
        }
    }

    // Function used to retrieve all the data about the user
    fun loadDataFriend(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val key = "user/$username.json"

            try {
                val request = GetObjectRequest(BuildConfig.BUCKET_NAME, key)
                val result = s3Client.getObject(request)
                val content = result.objectContent.bufferedReader().use { it.readText() }

                val gson = Gson()
                _friend.postValue(gson.fromJson(content, Friend::class.java))
            } catch (e: Exception) {
                Log.e("GROUP_VIEW_MODEL", e.toString())
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

    fun getUserVehicleTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _userVehicleTime.postValue(time)
    }

    fun getUserKilometersTravelled(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var meters = 0.0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if ((timeStamp.isAfter(start) && timeStamp.isBefore(end)) || timeStamp.isEqual(start)) {
                meters += it.distanceTravelled
            }
        }

        _userMetersTravelled.postValue(meters)
    }

    fun getUserVehicleBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
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

        _userVehicleBarChartEntries.postValue(entries)
    }

    fun getUserRunTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.runActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _userRunTime.postValue(time)
    }

    fun getUserKilometersRun(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var meters = 0.0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.runActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if ((timeStamp.isAfter(start) && timeStamp.isBefore(end)) || timeStamp.isEqual(start)) {
                meters += it.distanceDone
            }
        }

        _userMetersRun.postValue(meters)
    }

    fun getUserRunBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val vehicleItems = friend.runActivities

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

        _userRunBarChartEntries.postValue(entries)
    }

    fun getUserStillTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.stillActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end) && it.exitTime > it.arrivalTime) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _userStillTime.postValue(time)
    }

    fun getUserStillBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
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

        _userStillBarChartEntries.postValue(entries)
    }

    fun getUserWalkTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.walkActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end) && it.exitTime > it.arrivalTime) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _userWalkTime.postValue(time)
    }

    fun getUserSteps(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var steps = 0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.walkActivities.forEach {
            val timeStamp = LocalDateTime.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                steps += it.stepsNumber.toInt()
            }
        }

        _userSteps.postValue(steps)
    }

    fun getUserWalkBarChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
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

        _userWalkBarChartEntries.postValue(entries)
    }

    fun getUserVehicleLineChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
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

        _userVehicleLineChartEntries.postValue(entries)
    }

    fun getUserRunLineChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
        val firstDay = LocalDate.parse(startOfWeek, pattern)
        val lastDay = LocalDate.parse(endOfWeek, pattern)

        val groupedItems = friend.runActivities.groupBy {
            it.timestamp.let { timestamp ->
                LocalDate.parse(
                    timestamp,
                    pattern
                ).dayOfMonth
            }
        }

        val stepsPerDay = groupedItems.mapValues { it ->
            (it.value.sumOf { it.distanceDone } / TO_KM).toFloat()
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

        _userRunLineChartEntries.postValue(entries)
    }

    fun getUserWalkLineChartEntries(startOfWeek: String, endOfWeek: String, friend: Friend) {
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

        _userWalkLineChartEntries.postValue(entries)
    }

    fun getUserCountOfActivities(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        // I use scope function filter to avoid concurrent exceptions
        val vehicleActivities = friend.vehicleActivities
            .filter { it.exitTime > it.arrivalTime }
            .filter {
                val timeStamp = LocalDateTime.parse(it.timestamp, pattern)
                (timeStamp.isBefore(end) && timeStamp.isAfter(start))
            }

        val runActivities = friend.runActivities
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

        _userPieChartEntries.postValue(entries)
    }

    fun getUserGeofenceTransitions(friend: Friend) {
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

        _userGeofenceTransitions.postValue(list.filter { it.nameLocation.isNotBlank() })
    }

    fun addFriend(username: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (username != null) {
                db.FriendDao().insertFriend(FriendEntity(userId = MyUser.id, username = username))
                _errorAddFriend.postValue(true)
            } else {
                _errorAddFriend.postValue(false)
            }
        }
    }

    fun deleteFriend(username: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (username != null) {
                db.FriendDao().deleteFriend(MyUser.id, username)
                _errorDeleteFriend.postValue(true)
            } else {
                _errorDeleteFriend.postValue(false)
            }
        }
    }

    private fun loadDataUser(username: String?): User? {
        val key = "user/$username.json"

        var subscriber: User? = null

        try {
            val request = GetObjectRequest(BuildConfig.BUCKET_NAME, key)
            val result = s3Client.getObject(request)
            val content = result.objectContent.bufferedReader().use { it.readText() }

            val gson = Gson()
            subscriber = gson.fromJson(content, User::class.java)
        } catch (e: Exception) {
            Log.e("GROUP_VIEW_MODEL", e.toString())
        }

        return subscriber
    }

    // Function used to convert time stamp format from String to LocalDateTime
    private fun getLocalDateFromTimeStamp(startOfPeriod: String, endOfPeriod: String): Pair<LocalDateTime, LocalDateTime> {
        val start = LocalDateTime.parse(startOfPeriod, pattern)
        val end = LocalDateTime.parse(endOfPeriod, pattern)

        return Pair(start, end)
    }
}