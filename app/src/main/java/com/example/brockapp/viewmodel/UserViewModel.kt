package com.example.brockapp.viewmodel

import com.example.brockapp.BUCKET_NAME
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

    fun registerUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAlreadyExists = checkIfUserExistsOnS3(username)

            if (userAlreadyExists) {
                _auth.postValue(false)
            } else {
                db.UserDao().insertUser(UserEntity(username = username, password = password, sharingFlag = false))

                val file = createUserDataFile(username)
                uploadUserToS3(username, file)

                _auth.postValue(true)
            }
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

    private fun uploadUserToS3(username: String, file: File) {
        val userKey = "user/$username.json"

        try {
            val request = PutObjectRequest(BUCKET_NAME, userKey, file)
            s3Client.putObject(request)
        } catch (e: Exception) {
            Log.e("UploadError", "Errore durante l'upload del file su S3", e)
            throw e
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

    fun changeSharingDataFlag(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.UserDao().changeFlag(username, password)
        }
    }
}