package com.example.brockapp.authenticator

import android.content.SharedPreferences

open class BuiltInAuthenticator {
    public fun addCredentials(username: String, password: String, spAuth: SharedPreferences?) {
        with(spAuth!!.edit()) {
            putString(username, password)
            apply()
        }
    }

    public fun authCredentials(username : String, password : String, spAuth : SharedPreferences?) : Boolean {
        return (username == spAuth?.getString(username, "") && password == spAuth?.getString(password, ""))
    }
}