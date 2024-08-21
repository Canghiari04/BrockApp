package com.example.brockapp.viewmodel

import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserEntity

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class UserViewModel(private val db: BrockDB): ViewModel() {
    private var _auth = MutableLiveData<Boolean>()
    val auth: LiveData<Boolean> get() = _auth

    fun registerUser(username: String, password: String) {
        viewModelScope.launch {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)

            if (userAlreadyExists) {
                _auth.value = false
            } else {
                db.UserDao().insertUser(UserEntity(username = username, password = password))
                _auth.value = true
            }
        }
    }

    fun checkIfUserExists(username: String, password: String) {
        viewModelScope.launch {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)
            _auth.value = userAlreadyExists
        }
    }
}