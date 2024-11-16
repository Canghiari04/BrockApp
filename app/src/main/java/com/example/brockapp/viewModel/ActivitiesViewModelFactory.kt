package com.example.brockapp.viewmodel

import com.example.brockapp.room.BrockDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ActivitiesViewModelFactory(private val brockDB: BrockDB): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivitiesViewModel(brockDB) as T
    }
}