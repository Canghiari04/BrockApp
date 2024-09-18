package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.data.Friend
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.data.UserActivity
import com.example.brockapp.database.FriendEntity

import java.io.File
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.example.brockapp.data.Locality

class FriendsViewModel(private val s3Client: AmazonS3Client, private val db: BrockDB, private val file: File): ViewModel() {
    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> get() = _friends

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> get() = _suggestions

    private val _errorAddFriend = MutableLiveData<Boolean>()
    val errorAddFriend: LiveData<Boolean> = _errorAddFriend

    private val _friendGeofenceLocalities = MutableLiveData<List<Locality>>()
    val friendGeofenceLocalities: LiveData<List<Locality>> = _friendGeofenceLocalities

    private val _friendActivities = MutableLiveData<List<UserActivity>>()
    val friendActivities: LiveData<List<UserActivity>> = _friendActivities

    fun uploadUserData() {
        viewModelScope.launch(Dispatchers.Default) {
            val geofence = db.GeofenceAreaDao().getAllGeofenceAreas()
            val walkActivities = db.UserWalkActivityDao().getWalkActivitiesByUserId(User.id)
            val vehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserId(User.id)
            val stillActivities = db.UserStillActivityDao().getStillActivitiesByUserId(User.id)

            val userData = mapOf(
                "username" to User.username,
                "walkActivities" to walkActivities,
                "vehicleActivities" to vehicleActivities,
                "stillActivities" to stillActivities,
                "geofence" to geofence
            )

            val gson = Gson()
            val json = gson.toJson(userData)

            file.writeText(json)

            withContext(Dispatchers.IO) {
                try {
                    val request = PutObjectRequest(BUCKET_NAME, "user/${User.username}.json", file)
                    s3Client.putObject(request)
                } catch (e: Exception) {
                    Log.e("S3Upload", "Failed to upload user data", e)
                }
            }
        }
    }

    fun getCurrentFriends(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = db.FriendDao().getFriendsByUserId(id)
            _friends.postValue(item)
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
                        it.key.endsWith(".json") && it.key != "user/${User.username}.json"
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

    fun addFriend(user: String) {
        val listCurrentUsernameFriends = _friends.value

        viewModelScope.launch(Dispatchers.IO) {
            if (listCurrentUsernameFriends?.contains(user) == false) {
                val friend = FriendEntity(userId = User.id, followedUsername = user)
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

    fun getFriendData(username: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val friend = loadFriendData(username)
            val listGeofence = ArrayList<Locality>()
            val listActivities = ArrayList<UserActivity>()

            friend?.geofence?.forEach {
                val newLocality = Locality(it.name, it.longitude, it.latitude)
                listGeofence.add(newLocality)
            }

            friend?.stillActivities?.parallelStream()?.forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, STILL_ACTIVITY_TYPE, "")
                listActivities.add(newActivity)
            }

            friend?.vehicleActivities?.parallelStream()?.forEach {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, VEHICLE_ACTIVITY_TYPE, it.distanceTravelled.toString())
                listActivities.add(newActivity)
            }

            friend?.walkActivities?.parallelStream()?.forEach  {
                val newActivity = UserActivity(it.id, it.userId, it.timestamp, it.transitionType, WALK_ACTIVITY_TYPE, it.stepNumber.toString())
                listActivities.add(newActivity)
            }

            _friendGeofenceLocalities.postValue(listGeofence)
            _friendActivities.postValue(listActivities.sortedBy { it.timestamp })
        }
    }

    // Suspend function cause called by a Coroutine
    private suspend fun loadFriendData(username: String?): Friend? {
        val friendKey = "user/$username.json"

        return try {
            withContext(Dispatchers.Default) {
                val request = GetObjectRequest(BUCKET_NAME, friendKey)
                val result = s3Client.getObject(request)
                val content = result.objectContent.bufferedReader().use { it.readText() }

                val gson = Gson()
                gson.fromJson(content, Friend::class.java)
            }
        } catch (e: Exception) {
            Log.e("FRIENDS_VIEW_MODEL", e.toString())
            null
        }
    }
}