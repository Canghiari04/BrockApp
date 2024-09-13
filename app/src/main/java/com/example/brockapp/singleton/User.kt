package com.example.brockapp.singleton

object User {
    var id: Long = 0
    var username: String = ""
    var password: String = ""
    var flag: Boolean = false

    fun logoutUser() {
        id = 0
        username = ""
        password = ""
        flag = false
    }
}