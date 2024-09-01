package com.example.brockapp.viewmodel

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class NetworkViewModel: ViewModel() {
    private val _currentNetwork = MutableLiveData<Boolean>()
    val currentNetwork: LiveData<Boolean> get() = _currentNetwork

    fun setNetwork(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            _currentNetwork.value = enabled
        }
    }
}