package com.example.brockapp.util

import android.content.Context

class SharedPreferencesUtil {
    fun getPreference(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }
}