package com.example.brockapp.viewModel

import com.example.brockapp.data.Area

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import android.location.Geocoder
import org.osmdroid.util.GeoPoint
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.FlowPreview
import androidx.lifecycle.viewModelScope

class MapViewModel(private val geocoder: Geocoder): ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _suggestions = MutableStateFlow<Map<String, Area>?>(null)
    @OptIn(FlowPreview::class)
    val suggestions = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_suggestions) { text, _ ->
            if (text.isNotBlank()) {
                delay(2000L)
                defineSuggestions(text)
            } else {
                null
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _suggestions.value
        )

    fun onSearchTextChange(item: String) {
        _searchText.value = item
    }

    private fun defineSuggestions(query: String): Map<String, Area> {
        try {
            val suggestions: MutableMap<String, Area> = mutableMapOf()
            val addresses = geocoder.getFromLocationName(query.lowercase(), 10)

            addresses?.let {
                for (address in it) {
                    suggestions.put(
                        address.getAddressLine(0),
                        Area(
                            address,
                            GeoPoint(
                                address.latitude,
                                address.longitude
                            )
                        )
                    )
                }
            }
            return suggestions
        } catch (e: Exception) {
            return mutableMapOf()
        }
    }
}