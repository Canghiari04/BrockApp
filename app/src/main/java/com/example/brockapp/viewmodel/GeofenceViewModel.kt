package com.example.brockapp.viewmodel

import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.GeofenceAreaEntity

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class GeofenceViewModel(private val db: BrockDB): ViewModel() {
    private val _staticAreas = MutableLiveData<List<GeofenceAreaEntity>>()
    val staticAreas: LiveData<List<GeofenceAreaEntity>> get() = _staticAreas

    private val _dynamicAreas = MutableLiveData<List<GeofenceAreaEntity>>()
    val dynamicAreas: LiveData<List<GeofenceAreaEntity>> get() = _dynamicAreas

    fun insertStaticGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.GeofenceAreaDao().countAllGeofenceAreas()

            // Check if the user have already geofence inside the database
            if (count == 0) {
                val geofenceAreas = listOf(
                    GeofenceAreaEntity(longitude = 11.352396, latitude = 44.482086, name = "Giardini Margherita"),
                    GeofenceAreaEntity(longitude = 11.346302, latitude = 44.502505, name = "Parco della Montagnola"),
                    GeofenceAreaEntity(longitude = 11.326957, latitude = 44.476543, name = "Villa Ghigi")
                )

                for (area in geofenceAreas) {
                    db.GeofenceAreaDao().insertGeofenceArea(area)
                }

                fetchGeofenceAreas()
            }
        }
    }

    fun fetchGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.GeofenceAreaDao().getAllGeofenceAreas()
            _staticAreas.postValue(items)
        }
    }

    fun insertGeofenceArea(area: GeofenceAreaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.GeofenceAreaDao().insertGeofenceArea(area)
            fetchDynamicGeofenceAreas()
        }
    }

    fun deleteGeofenceArea(name: String?, longitude: Double, latitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            db.GeofenceAreaDao().deleteGeofenceArea(name, longitude, latitude)
            fetchDynamicGeofenceAreas()
        }
    }

    private fun fetchDynamicGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.GeofenceAreaDao().getAllGeofenceAreas()
            _dynamicAreas.postValue(items)
        }
    }
}