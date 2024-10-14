package com.example.brockapp.interfaces

import android.content.Context

interface ShowCustomToast {
    fun showBasicToast(message: String, context: Context)

    fun showWarningToast(message: String, context: Context)
}