package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.data.Friend
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB

import java.io.File
import android.util.Log
import com.google.gson.Gson
import android.content.Context
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.example.brockapp.database.FriendEntity

class FriendsViewModel(private val s3Client: AmazonS3Client, private val db: BrockDB, private val context: Context) : ViewModel() {
    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> get() = _friends

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> get() = _suggestions

    // Utente ricercato dal EditText.
    private val _newUser = MutableLiveData<String>()
    val newUser: LiveData<String> = _newUser

    private val _errorAddFriend = MutableLiveData<Boolean>()
    val errorAddFriend: LiveData<Boolean> = _errorAddFriend

    fun getCurrentFriends(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = db.FriendDao().getFriendsByUserId(id)
            _friends.postValue(item)
        }
    }

    fun getSuggestionFriend() {

    }

    /**
     * Metodo necessario per inserire i dati dell'utente all'interno della repository in cloud.
     */
    fun uploadUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            val walkActivities = db.UserWalkActivityDao().getWalkActivitiesByUserId(User.id)
            val vehicleActivities = db.UserVehicleActivityDao().getVehicleActivitiesByUserId(User.id)
            val stillActivities = db.UserStillActivityDao().getStillActivitiesByUserId(User.id)

            val userData = mapOf(
                "username" to User.username,
                "walkActivities" to walkActivities,
                "vehicleActivities" to vehicleActivities,
                "stillActivities" to stillActivities
            )

            val gson = Gson()
            val json = gson.toJson(userData)

            val file = File(context.filesDir, "user_data.json")
            file.writeText(json)

            val thread = Thread {
                try {
                    val request = PutObjectRequest(BUCKET_NAME, "user/${User.username}.json", file)
                    s3Client.putObject(request)
                } catch (e: Exception) {
                    Log.e("S3Upload", "Failed to upload user data", e)
                }
            }

            thread.start()
        }
    }

    fun searchUser(user: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userKey = "user/$user.json"

                val listObjectsRequest = ListObjectsRequest()
                    .withBucketName(BUCKET_NAME)
                    .withPrefix(userKey)
                    .withMaxKeys(1)

                val objectListing = s3Client.listObjects(listObjectsRequest)
                val s3Objects = objectListing.objectSummaries

                if (s3Objects.isNotEmpty() && s3Objects[0].key == userKey) {
                    _newUser.postValue(user)
                } else {
                    _newUser.postValue("")
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

    suspend fun loadFriendData(username: String): Friend? {
        val friendKey = "user/$username.json"

        return try {
            withContext(Dispatchers.IO) {
                val request = GetObjectRequest(BUCKET_NAME, friendKey)
                val result = s3Client.getObject(request)
                val content = result.objectContent.bufferedReader().use { it.readText() }
                val gson = Gson()
                gson.fromJson(content, Friend::class.java)
            }
        } catch (e: Exception) {
            Log.e("FRIENDS_VIEW_MODEL", "Failed to load friend data for $username", e)
            null
        }
    }
}