package com.example.brockapp.data

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

    /**
     * Metodo attuato per "distruggere" l'istanza del Singleton dopo aver effettuato il logout
     * dall'applicazione
     */
    fun logoutUser(user: User) {
        user.id = 0
        user.username = ""
        user.password = ""
    }
}