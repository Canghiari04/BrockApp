package com.example.brockapp.singleton

object User {
    private val instance = User

    var id: Long = 0
    var username: String = ""
    var password: String = ""

    fun getInstance(): User {
        return instance
    }

    /**
     * Metodo attuato per "distruggere" l'istanza del Singleton dopo aver effettuato il logout
     * dall'applicazione
     */
    fun logoutUser(user: User) {
        id = 0
        username = ""
        password = ""
    }
}