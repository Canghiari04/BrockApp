package com.example.brockapp.viewModel

import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class MapViewModel(private val geocoder: Geocoder): ViewModel() {

    private val _searchLocation = MutableStateFlow("")
    val searchLocation = _searchLocation.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()
    
    private val _locations = MutableStateFlow(listOf<Location>())
    val locations = searchLocation
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_locations) { text, _ ->
            if (text.isNotBlank()) {
                delay(2000L)
                val addresses = geocoder.getFromLocationName(query, 10)
            }
        }

    fun onSearchLocationChange(text: String) {
        _searchLocation.value = text
    }
}