package com.example.brockapp.viewmodel

import com.example.brockapp.singleton.MyNetwork

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class NetworkViewModel: ViewModel() {
    private val _currentNetwork = MutableLiveData<Boolean>()
    val currentNetwork: LiveData<Boolean> get() = _currentNetwork

    init {
        _currentNetwork.value = MyNetwork.isConnected
    }

    fun setNetwork(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            if (_currentNetwork.value != enabled) {
                _currentNetwork.value = enabled
            }
        }
    }
}