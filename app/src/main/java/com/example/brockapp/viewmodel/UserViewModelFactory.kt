package com.example.brockapp.viewmodel

import android.content.Context
import com.example.brockapp.database.BrockDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UserViewModelFactory(private val brockDB: BrockDB, private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(brockDB, context) as T
    }
}