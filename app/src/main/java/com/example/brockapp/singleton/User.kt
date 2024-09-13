package com.example.brockapp.singleton

object User {
    var id: Long = 0
    var username: String = ""
    var password: String = ""
    var recognition: Boolean = false
    var sharing: Boolean = false

    fun logoutUser() {
        id = 0
        username = ""
        password = ""
        recognition = false
        sharing = false
    }
}