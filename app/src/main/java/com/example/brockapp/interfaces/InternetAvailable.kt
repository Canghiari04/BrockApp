package com.example.brockapp.interfaces

import android.content.Context

interface InternetAvailable {
    fun isInternetActive(context: Context): Boolean
}