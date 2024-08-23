package com.example.brockapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.GeofenceAreaEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceViewModel(private val db: BrockDB) : ViewModel() {
    private val _areas = MutableLiveData<List<GeofenceAreaEntry>>()
    val areas: LiveData<List<GeofenceAreaEntry>> get() = _areas

    private val _dynamicAreas = MutableLiveData<List<GeofenceAreaEntry>>()
    val dynamicAreas: LiveData<List<GeofenceAreaEntry>> get() = _dynamicAreas

    init {
        fetchGeofenceAreas()
    }

    fun insertStaticGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.GeofenceAreaDao().countAllGeofenceAreas()

            if (count == 0) {
                val geofenceAreas = listOf(
                    GeofenceAreaEntry(longitude = 11.352396, latitude = 44.482086, name = "Giardini Margherita"),
                    GeofenceAreaEntry(longitude = 11.346302, latitude = 44.502505, name = "Parco della Montagnola"),
                    GeofenceAreaEntry(longitude = 11.326957, latitude = 44.476543, name = "Villa Ghigi")
                )

                for(area in geofenceAreas) {
                    db.GeofenceAreaDao().insertGeofenceArea(area)
                }

                fetchGeofenceAreas()
            }
        }
    }

    fun insertGeofenceArea(area: GeofenceAreaEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            db.GeofenceAreaDao().insertGeofenceArea(area)
            fetchDynamicGeofenceAreas()
        }
    }

    private fun fetchGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.GeofenceAreaDao().getAllGeofenceAreas()
            _areas.postValue(items)
        }
    }

    private fun fetchDynamicGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.GeofenceAreaDao().getAllGeofenceAreas()
            _dynamicAreas.postValue(items)
        }
    }
}