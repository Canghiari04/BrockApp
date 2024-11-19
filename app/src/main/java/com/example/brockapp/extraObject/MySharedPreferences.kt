package com.example.brockapp.extraObject

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.DetectedActivity

object MySharedPreferences {
    private const val PREFERENCES_NAME = "MY_APP_PREFERENCES"
    private const val KEY_USERNAME = "USERNAME"
    private const val KEY_PASSWORD = "PASSWORD"

    fun getCredentialsSaved(context: Context): Pair<String?, String?> {
        val sharedPreferences = getSharedPreferences(context)

        return Pair(
            sharedPreferences.getString(KEY_USERNAME, null),
            sharedPreferences.getString(KEY_PASSWORD, null)
        )
    }

    fun getActivity(key: String, context: Context): Int {
        return getSharedPreferences(context).getInt(key, DetectedActivity.UNKNOWN)
    }

    fun checkService(key: String, context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(key, false)
    }

    fun setCredentials(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putString(KEY_USERNAME, MyUser.username)
            putString(KEY_PASSWORD, MyUser.password)
            apply()
        }
    }

    fun setService(key: String, item: Boolean, context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putBoolean(key, item)
            apply()
        }
    }

    fun setActivity(key: String, item: Int, context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putInt(key, item)
            apply()
        }
    }

    fun deleteSavedPreferences(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
}