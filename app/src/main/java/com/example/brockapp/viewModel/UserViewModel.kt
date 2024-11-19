package com.example.brockapp.viewModel

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.room.UsersEntity
import com.example.brockapp.data.CityResponse
import com.example.brockapp.singleton.MySupabase
import com.example.brockapp.retrofit.RetrofitClientInstance

import java.io.File
import retrofit2.Call
import android.util.Log
import retrofit2.Callback
import retrofit2.Response
import java.io.FileWriter
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.google.gson.JsonObject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import io.github.jan.supabase.postgrest.from
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest

class UserViewModel(private val db: BrockDB, private val s3Client: AmazonS3Client, private val file: File): ViewModel() {

    private var _user = MutableLiveData<UsersEntity>()
    val user: LiveData<UsersEntity> get() = _user

    private var _auth = MutableLiveData<Boolean>()
    val auth: LiveData<Boolean> get() = _auth

    private var _cities = MutableLiveData<List<String>>()
    val cities: LiveData<List<String>> get() = _cities

    private var _recording = MutableLiveData<Boolean>()
    val recording: LiveData<Boolean> get() = _recording

    fun getUserFromRoom(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = db.UsersDao().getUserFromUsernameAndPassword(username, password)
            _user.postValue(user)
        }
    }

    fun getUserFromSupabase(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = MySupabase.getInstance().from("Users").select {
                    filter {
                        eq("username", username)
                    }
                    filter {
                        eq("password", password)
                    }
                }.decodeSingle<UsersEntity>()

                _user.postValue(user)
                _auth.postValue(true)
            } catch (e: Exception) {
                _auth.postValue(false)
            }
        }
    }

    fun getCitiesFromCountry(countryCode: String) {
        RetrofitClientInstance.api
            .getCitiesByCountryId(BuildConfig.GEO_DB_API_KEY, countryCode)
            .enqueue(object : Callback<CityResponse> {
                override fun onResponse(call: Call<CityResponse>, response: Response<CityResponse>) {
                    val items = response.body()?.data?.map { it.name } ?: mutableListOf()
                    _cities.postValue(items)
                }

                override fun onFailure(call: Call<CityResponse>, t: Throwable) { }
            })
    }

    fun registerUser(username: String, password: String, typeActivity: String, country: String, city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAlreadyExists = MySupabase.getInstance()
                .from("Users")
                .select {
                    filter { eq("username", username)
                    }
                }.decodeSingleOrNull<UsersEntity>()

            if (userAlreadyExists != null) {
                _auth.postValue(false)
            } else {
                val user = UsersEntity(
                    username = username,
                    password = password,
                    typeActivity = typeActivity,
                    country = country,
                    city = city
                )

                db.UsersDao().insertUser(user)

                _user.postValue(user)
                _auth.postValue(true)
            }
        }
    }

    fun registerUserToS3(username: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (existUserOnS3(username)) {
                _recording.postValue(true)
            } else {
                try {
                    uploadUserToS3(
                        username,
                        createUserDataFile(username)
                    )

                    _recording.postValue(true)
                } catch (e: Exception) {
                    _recording.postValue(false)
                }
            }
        }
    }

    private fun existUserOnS3(username: String): Boolean {
        val userKey = "user/$username.json"

        return try {
            val request = GetObjectRequest(BuildConfig.BUCKET_NAME, userKey)
            s3Client.getObject(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createUserDataFile(username: String): File {
        try {
            val gson = Gson()
            val jsonObject = JsonObject()
            jsonObject.addProperty("username", username)

            FileWriter(file).use { writer ->
                writer.write(gson.toJson(jsonObject))
            }
        } catch (e: Exception) {
            Log.e("USER_VIEW_MODEL", e.toString())
        }

        return file
    }

    private fun uploadUserToS3(username: String, jsonFile: File) {
        val userKey = "user/$username.json"

        try {
            val request = PutObjectRequest(BuildConfig.BUCKET_NAME, userKey, jsonFile)
            s3Client.putObject(request)
        } catch (e: Exception) {
            Log.e("USER_VIEW_MODEL", e.toString())
        }
    }
}