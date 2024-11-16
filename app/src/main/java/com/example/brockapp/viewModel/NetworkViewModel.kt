package com.example.brockapp.viewmodel

import com.example.brockapp.extraObject.MyNetwork

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class NetworkViewModel: ViewModel() {
    private val _currentNetwork = MutableLiveData<Boolean>()
    val currentNetwork: LiveData<Boolean> get() = _currentNetwork

    init {
        _currentNetwork.postValue(MyNetwork.isConnected)
    }

    fun setNetwork(enabled: Boolean) {
        if (_currentNetwork.value != enabled) {
            _currentNetwork.postValue(enabled)
        }
    }
}