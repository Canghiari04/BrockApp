package com.example.brockapp.viewmodel

import com.example.brockapp.extraObject.MyNetwork

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class NetworkViewModel: ViewModel() {
    private val _authNetwork = MutableLiveData<Boolean>()
    val authNetwork: LiveData<Boolean> get() = _authNetwork

    private val _currentNetwork = MutableLiveData<Boolean>()
    val currentNetwork: LiveData<Boolean> get() = _currentNetwork

    init {
        _currentNetwork.value = MyNetwork.isConnected
        _authNetwork.value = MyNetwork.isConnected
    }

    fun setAuthNetwork(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            if (_authNetwork.value != enabled) {
                _authNetwork.value = enabled
            }
        }
    }

    fun setNetwork(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            if (_currentNetwork.value != enabled) {
                _currentNetwork.value = enabled
            }
        }
    }
}