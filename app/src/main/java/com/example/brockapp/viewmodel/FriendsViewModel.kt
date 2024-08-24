package com.example.brockapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.brockapp.BUCKET_NAME
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FriendsViewModel(private val s3Client: AmazonS3Client, private val db: BrockDB, private val context: Context): ViewModel() {
    private val _friends = MutableLiveData<List<Friend>>()
    val friends: LiveData<List<Friend>> = _friends


    /*
    fun updateFriendsData() {

        // Launching a coroutine to perform network I/O
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // List objects in the user/ directory
                val listObjectsRequest = ListObjectsRequest()
                    .withBucketName(BUCKET_NAME)
                    .withPrefix("user/")

                val objectListing = s3Client.listObjects(listObjectsRequest)
                val s3Objects = objectListing.objectSummaries

                val friendsList = mutableListOf<Friend>()

                for (summary: S3ObjectSummary in s3Objects) {
                    if (summary.key.endsWith(".json")) {
                        val s3Object = s3Client.getObject(BUCKET_NAME, summary.key)
                        val content = s3Object.objectContent

                        val reader = BufferedReader(InputStreamReader(content))
                        val jsonBuilder = StringBuilder()
                        var line: String? = reader.readLine()

                        while (line != null) {
                            jsonBuilder.append(line)
                            line = reader.readLine()
                        }

                        reader.close()
                        content.close()

                        val json = JSONObject(jsonBuilder.toString())
                        val friend = parseFriendFromJson(json)
                        friendsList.add(friend)
                    }
                }

                // Update the LiveData with the fetched data
                _friends.postValue(friendsList)
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Failed to retrieve data", e)
            }
        }
    }

    private fun parseFriendFromJson(json: JSONObject): Friend {
        val username = json.getString("username")

        val walkActivities = mutableListOf<WalkActivity>()
        val vehicleActivities = mutableListOf<VehicleActivity>()
        val stillActivities = mutableListOf<StillActivity>()

        val walkArray = json.optJSONArray("walkActivities")
        val vehicleArray = json.optJSONArray("vehicleActivities")
        val stillArray = json.optJSONArray("stillActivities")

        walkArray?.let {
            for (i in 0 until it.length()) {
                var walkActivity = WalkActivity()
                walkActivities.add(WalkActivity())
            }
        }

        vehicleArray?.let {
            for (i in 0 until it.length()) {
                vehicleActivities.add(VehicleActivity(it.getJSONObject(i)))
            }
        }

        stillArray?.let {
            for (i in 0 until it.length()) {
                stillActivities.add(StillActivity(it.getJSONObject(i)))
            }
        }

        return Friend(username, walkActivities, vehicleActivities, stillActivities)
    }

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
                    Log.d("S3Upload", "User data uploaded successfully")
                } catch (e: Exception) {
                    Log.e("S3Upload", "Failed to upload user data", e)
                }
            }
            thread.start()
        }
    }

}
