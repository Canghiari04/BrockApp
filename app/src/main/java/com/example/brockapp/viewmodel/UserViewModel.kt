package com.example.brockapp.viewmodel

import com.example.brockapp.User
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

    fun authSignIn(username: String, password: String) {
        viewModelScope.launch {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)

            if(userAlreadyExists) {
                _auth.value = false
            } else {
                db.UserDao().insertUser(UserEntity(username = username, password = password))

                val user = User.getInstance()

                user.id = db.UserDao().getIdFromUsernameAndPassword(username, password)
                user.username = username
                user.password = password

                _auth.value = true
            }
        }
    }

    fun authLogin(username: String, password: String) {
        viewModelScope.launch {
            val userAlreadyExists = db.UserDao().checkIfUserIsPresent(username, password)

            if(userAlreadyExists) {
                val user = User.getInstance()

                user.id = db.UserDao().getIdFromUsernameAndPassword(username, password)
                user.username = username
                user.password = password

                _auth.value = true
            } else {
                _auth.value = false
            }
        }
    }
}