package com.example.brockapp.viewmodel

import com.example.brockapp.extraObject.MyNetwork

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class NetworkViewModel: ViewModel() {
    private val _authNetwork = MutableLiveData<Boolean>()
    val authNetwork: LiveData<Boolean> get() = _authNetwork

    private val _currentNetwork = MutableLiveData<Boolean>()
    val currentNetwork: LiveData<Boolean> get() = _currentNetwork

    init {
        _currentNetwork.postValue(MyNetwork.isConnected)
        _authNetwork.postValue(MyNetwork.isConnected)
    }

    fun setAuthNetwork(enabled: Boolean) {
        if (_authNetwork.value != enabled) {
            _authNetwork.postValue(enabled)
        }
    }

    fun setNetwork(enabled: Boolean) {
        if (_currentNetwork.value != enabled) {
            _currentNetwork.postValue(enabled)
        }
    }
}