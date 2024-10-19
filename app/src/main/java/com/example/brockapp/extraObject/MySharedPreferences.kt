package com.example.brockapp.extraObject

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.DetectedActivity

object MySharedPreferences {
    private const val PREFERENCES_NAME = "MY_APP_PREFERENCES"
    private const val KEY_ID = "ID"
    private const val KEY_USERNAME = "USERNAME"
    private const val KEY_PASSWORD = "PASSWORD"
    private const val KEY_ACTIVITY_RECOGNITION = "ACTIVITY_RECOGNITION"
    private const val KEY_GEOFENCE_TRANSITION = "GEOFENCE_TRANSITION"
    private const val KEY_DUMP_DATABASE = "DUMP_DATABASE"
    private const val KEY_STILL_ACTIVITY = "STILL_ACTIVITY"
    private const val KEY_VEHICLE_ACTIVITY = "VEHICLE_ACTIVITY"
    private const val KEY_WALK_ACTIVITY = "WALK_ACTIVITY"
    private const val KEY_RUN_ACTIVITY = "RUN_ACTIVITY"

    fun setUpSharedPreferences(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putLong(KEY_ID, MyUser.id)
            putString(KEY_USERNAME, MyUser.username)
            putString(KEY_PASSWORD, MyUser.password)
            putBoolean(KEY_ACTIVITY_RECOGNITION, false)
            putBoolean(KEY_GEOFENCE_TRANSITION, false)
            putBoolean(KEY_DUMP_DATABASE, false)
            putInt(KEY_STILL_ACTIVITY, DetectedActivity.UNKNOWN)
            putInt(KEY_VEHICLE_ACTIVITY, DetectedActivity.UNKNOWN)
            putInt(KEY_WALK_ACTIVITY, DetectedActivity.UNKNOWN)
            putInt(KEY_RUN_ACTIVITY, DetectedActivity.UNKNOWN)
            apply()
        }
    }

    fun getCredentialsSaved(context: Context): Triple<Long, String?, String?> {
        val sharedPreferences = getSharedPreferences(context)

        return Triple(
            sharedPreferences.getLong(KEY_ID, 0L),
            sharedPreferences.getString(KEY_USERNAME, null),
            sharedPreferences.getString(KEY_PASSWORD, null)
        )
    }

    fun setCredentialsSaved(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putLong(KEY_ID, MyUser.id)
            putString(KEY_USERNAME, MyUser.username)
            putString(KEY_PASSWORD, MyUser.password)
            apply()
        }
    }

    fun checkService(key: String, context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)

        return sharedPreferences.getBoolean(key, false)
    }

    fun setService(key: String, item: Boolean, context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putBoolean(key, item)
            apply()
        }
    }

    fun getActivity(key: String, context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)

        return sharedPreferences.getInt(key, DetectedActivity.UNKNOWN)
    }

    fun setActivity(key: String, item: Int, context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            putInt(key, item)
            apply()
        }
    }

    fun logout(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            apply()
        }
    }

    fun deleteAll(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        with(sharedPreferences.edit()) {
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            remove(KEY_ACTIVITY_RECOGNITION)
            remove(KEY_GEOFENCE_TRANSITION)
            remove(KEY_DUMP_DATABASE)
            apply()
        }
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
}