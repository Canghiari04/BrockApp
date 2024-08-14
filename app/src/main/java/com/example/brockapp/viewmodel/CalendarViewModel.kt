package com.example.brockapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CalendarViewModel(): ViewModel() {
    private var _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    fun changeDate(item: String) {
        _date.value = item
    }
}