package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.FriendEntity
import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.GeofenceTransitionEntity
import com.example.brockapp.database.UserVehicleActivityEntity

import android.util.Log
import java.time.LocalDate
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import java.time.format.DateTimeFormatter
import androidx.lifecycle.MutableLiveData
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest

class GroupViewModel(private val s3Client: AmazonS3Client, private val db: BrockDB): ViewModel() {
    private val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val _users = MutableLiveData<List<String>>()
    val users: LiveData<List<String>> get() = _users

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> get() = _suggestions

    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> get() = _friends

    private val _friend = MutableLiveData<Friend>()
    val friend: LiveData<Friend> get() = _friend

    private val _mapFriendCountActivities = MutableLiveData<Map<String, Int>>()
    val mapFriendCountActivities: MutableLiveData<Map<String, Int>> get() = _mapFriendCountActivities

    private val _listFriendVehicleActivities = MutableLiveData<List<UserVehicleActivityEntity>>()
    val listFriendVehicleActivities: LiveData<List<UserVehicleActivityEntity>> get() = _listFriendVehicleActivities

    private val _friendMeters = MutableLiveData<Double>()
    val friendMeters: LiveData<Double> get() = _friendMeters

    private val _friendVehicleTime = MutableLiveData<Long>()
    val friendVehicleTime: LiveData<Long> get() = _friendVehicleTime

    private val _listFriendWalkActivities = MutableLiveData<List<UserWalkActivityEntity>>()
    val listFriendWalkActivities: LiveData<List<UserWalkActivityEntity>> get() = _listFriendWalkActivities

    private val _friendSteps = MutableLiveData<Int>()
    val friendSteps: LiveData<Int> get() = _friendSteps

    private val _friendWalkTime = MutableLiveData<Long>()
    val friendWalkTime: LiveData<Long> get() = _friendWalkTime

    private val _friendGeofenceTransitions = MutableLiveData<List<GeofenceTransitionEntity>>()
    val friendGeofenceTransitions: LiveData<List<GeofenceTransitionEntity>> = _friendGeofenceTransitions

    private val _errorAddFriend = MutableLiveData<Boolean>()
    val errorAddFriend: LiveData<Boolean> = _errorAddFriend

    fun getAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val listObjectsRequest = ListObjectsRequest()
                .withBucketName(BUCKET_NAME)

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
        viewModelScope.launch(Dispatchers.IO) {
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

    fun getCurrentFriends(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = db.FriendDao().getFriendsByUserId(id)
            _friends.postValue(item)
        }
    }

    fun loadData(username: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            val friendKey = "user/$username.json"

            try {
                withContext(Dispatchers.Default) {
                    val request = GetObjectRequest(BUCKET_NAME, friendKey)
                    val result = s3Client.getObject(request)
                    val content = result.objectContent.bufferedReader().use { it.readText() }

                    val gson = Gson()
                    _friend.postValue(gson.fromJson(content, Friend::class.java))
                }
            } catch (e: Exception) {
                Log.e("FRIENDS_VIEW_MODEL", e.toString())
            }
        }
    }

    fun getFriendCountOfActivities(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        var countStillActivities = 0

        friend.stillActivities.apply { removeAll { it.exitTime == 0L } }.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                countStillActivities++
            }
        }

        var countVehicleActivities = 0

        friend.vehicleActivities.apply { removeAll { it.exitTime == 0L } }.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                countVehicleActivities++
            }
        }

        var countWalkActivities = 0

        friend.walkActivities.apply { removeAll { it.exitTime == 0L } }.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                countWalkActivities++
            }
        }

        val map = mutableMapOf(
            "Still" to countStillActivities,
            "Vehicle" to countVehicleActivities,
            "Walk" to countWalkActivities
        )

        _mapFriendCountActivities.postValue(map)
    }

    fun getFriendVehicleActivities(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        val list = ArrayList<UserVehicleActivityEntity>()

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                val activity = UserVehicleActivityEntity(
                    it.id,
                    it.userId,
                    it.timestamp,
                    it.arrivalTime,
                    it.exitTime,
                    it.distanceTravelled
                )

                list.add(activity)
            }
        }

        _listFriendVehicleActivities.postValue(list)
    }

    fun getFriendKilometers(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var meters = 0.0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                meters += it.distanceTravelled
            }
        }

        _friendMeters.postValue(meters)
    }

    fun getFriendVehicleTime(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var time = 0L

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.vehicleActivities.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end) && it.exitTime > it.arrivalTime) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _friendVehicleTime.postValue(time)
    }

    fun getFriendWalkActivities(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        val list = ArrayList<UserWalkActivityEntity>()

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.walkActivities.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end)) {
                val activity = UserWalkActivityEntity(
                    it.id,
                    it.userId,
                    it.timestamp,
                    it.arrivalTime,
                    it.exitTime,
                    it.stepNumber
                )

                list.add(activity)
            }
        }

        _listFriendWalkActivities.postValue(list)
    }

    fun getFriendSteps(startOfPeriod: String, endOfPeriod: String, friend: Friend) {
        var steps = 0

        val (start, end) = getLocalDateFromTimeStamp(startOfPeriod, endOfPeriod)

        friend.walkActivities.forEach {
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

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
            val timeStamp = LocalDate.parse(it.timestamp, pattern)

            if (timeStamp.isAfter(start) && timeStamp.isBefore(end) && it.exitTime > it.arrivalTime) {
                time += (it.exitTime - it.arrivalTime)
            }
        }

        _friendWalkTime.postValue(time)
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

    fun addFriend(user: String) {
        val listCurrentUsernameFriends = _friends.value

        viewModelScope.launch(Dispatchers.IO) {
            if (listCurrentUsernameFriends?.contains(user) == false) {
                val friend = FriendEntity(userId = MyUser.id, followedUsername = user)
                db.FriendDao().insertFriend(friend)

                val updateList = listCurrentUsernameFriends.toMutableList()
                updateList.add(user)

                _friends.postValue(updateList)
                _errorAddFriend.postValue(true)
            } else {
                _errorAddFriend.postValue(false)
            }
        }
    }

    private fun getLocalDateFromTimeStamp(startOfPeriod: String, endOfPeriod: String): Pair<LocalDate, LocalDate> {
        val start = LocalDate.parse(startOfPeriod, pattern)
        val end = LocalDate.parse(endOfPeriod, pattern)

        return Pair(start, end)
    }
}