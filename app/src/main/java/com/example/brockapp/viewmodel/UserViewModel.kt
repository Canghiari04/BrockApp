package com.example.brockapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(private val db: BrockDB): ViewModel() {
    private var _auth = MutableLiveData<Boolean>()
    val auth: LiveData<Boolean> get() = _auth

    fun registerUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)

            if (userAlreadyExists) {
                _auth.postValue(false)
            } else {
                db.UserDao().insertUser(UserEntity(username = username, password = password, sharingFlag = false))
                _auth.postValue(true)
            }
        }
    }

    fun checkIfUserExists(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)
            _auth.postValue(userAlreadyExists)
        }
    }
}