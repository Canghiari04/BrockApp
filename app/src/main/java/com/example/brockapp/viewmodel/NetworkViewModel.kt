package com.example.brockapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brockapp.singleton.MyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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