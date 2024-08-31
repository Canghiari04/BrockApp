package com.example.brockapp.viewmodel

import android.content.Context
import android.util.Log
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserEntity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.brockapp.BUCKET_NAME
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class UserViewModel(private val db: BrockDB, private val context: Context): ViewModel() {
    private var _auth = MutableLiveData<Boolean>()
    val auth: LiveData<Boolean> get() = _auth

    private var _currentUser = MutableLiveData<UserEntity?>()
    val currentUser: LiveData<UserEntity?> get() = _currentUser

    private val credentialsProvider = CognitoCachingCredentialsProvider(
        context,
        "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
        Regions.EU_WEST_3
    )
    private val s3Client = AmazonS3Client(credentialsProvider)

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


    private fun createUserDataFile(username: String): File {
        val file = File(context.filesDir, "user_data.json")

        try {
            val gson = Gson()
            val jsonObject = JsonObject()
            jsonObject.addProperty("username", username)

            FileWriter(file).use { writer ->
                writer.write(gson.toJson(jsonObject))
            }
        } catch (e: Exception) {
            Log.e("FileCreationError", "Errore durante la creazione del file JSON", e)
            throw e
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