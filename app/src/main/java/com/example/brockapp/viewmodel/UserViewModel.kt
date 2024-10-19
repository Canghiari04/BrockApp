package com.example.brockapp.viewmodel

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserEntity

import java.io.File
import android.util.Log
import java.io.FileWriter
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.google.gson.JsonObject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest

class UserViewModel(private val db: BrockDB, private val s3Client: AmazonS3Client, private val file: File): ViewModel() {
    private var _auth = MutableLiveData<Boolean>()
    val auth: LiveData<Boolean> get() = _auth

    private var _currentUser = MutableLiveData<UserEntity?>()
    val currentUser: LiveData<UserEntity?> get() = _currentUser

    fun registerUser(username: String, password: String, typeActivity: String, country: String, city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAlreadyExistsOnS3 = checkIfUserExistsOnS3(username)

            if (userAlreadyExistsOnS3) {
                _auth.postValue(false)
            } else {
                db.UserDao().insertUser(
                    UserEntity(
                        username = username,
                        password = password,
                        typeActivity = typeActivity,
                        country = country,
                        city = city
                    )
                )

                val jsonFile = createUserDataFile(username)
                uploadUserToS3(username, jsonFile)

                _auth.postValue(true)
            }
        }
    }

    fun checkIfUserExistsLocally(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)
            _auth.postValue(userAlreadyExists)
        }
    }

    fun getUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = db.UserDao().getUserFromUsernameAndPassword(username, password)
            _currentUser.postValue(user)
        }
    }

    fun deleteUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentUser.postValue(null)
            db.UserDao().deleteUserByUsernameAndPassword(username, password)
        }
    }

    private fun checkIfUserExistsOnS3(username: String): Boolean {
        val userKey = "user/$username.json"

        return try {
            val request = GetObjectRequest(BUCKET_NAME, userKey)
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
            Log.e("FileCreationError", e.toString())
        }

        return file
    }

    // Here is defined the first upload inside the bucket
    private fun uploadUserToS3(username: String, jsonFile: File) {
        val userKey = "user/$username.json"

        try {
            val request = PutObjectRequest(BUCKET_NAME, userKey, jsonFile)
            s3Client.putObject(request)
        } catch (e: Exception) {
            Log.e("UploadError", e.toString())
        }
    }
}
