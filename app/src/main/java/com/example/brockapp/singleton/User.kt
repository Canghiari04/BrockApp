package com.example.brockapp.singleton

object User {
    private val instance = User

    var id: Long = 0
    var username: String = ""
    var password: String = ""
    var flag: Boolean = false

    fun getInstance(): User {
        return instance
    }

    fun logoutUser() {
        id = 0
        username = ""
        password = ""
        flag = false
    }
}