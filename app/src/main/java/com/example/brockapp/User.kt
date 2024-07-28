package com.example.brockapp

/**
 * Singoletto utilizzato per mantenere durante il run dell'applicazione il riferimento ad una sola istanza User.
 */
object User {
    private val instance = User

    var id: Long = 0
    var username: String = ""
    var password: String = ""

    fun getInstance() : User {
        return instance
    }
}