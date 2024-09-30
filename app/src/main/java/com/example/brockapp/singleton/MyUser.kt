package com.example.brockapp.singleton

object User {
    var id: Long = 0
    var username: String = ""
    var password: String = ""

    fun logoutUser() {
        id = 0
        username = ""
        password = ""
    }
}