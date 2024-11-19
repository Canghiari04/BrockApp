package com.example.brockapp.viewModel

import com.example.brockapp.room.BrockDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MemoViewModelFactory(private val brockDB: BrockDB): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MemoViewModel(brockDB) as T
    }
}