package com.example.brockapp.viewmodel

import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.GeofenceAreaEntry

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class GeofenceViewModel(private val db: BrockDB) : ViewModel() {
    private val _inserted = MutableLiveData<Boolean>()
    val insert: LiveData<Boolean> get() = _inserted

    private lateinit var areas: LiveData<List<GeofenceAreaEntry>>

    fun insertStaticGeofenceAreas() {
        viewModelScope.launch {
            val count = db.GeofenceAreaDao().countAllGeofenceAreas()

            if (count == 0) {
                val geofenceAreas = listOf(
                    GeofenceAreaEntry(longitude = 11.352396, latitude = 44.482086, name = "Giardini Margherita"),
                    GeofenceAreaEntry(longitude = 11.346302, latitude = 44.502505, name = "Parco della Montagnola"),
                    GeofenceAreaEntry(longitude = 11.326957, latitude = 44.476543, name = "Villa Ghigi"),
                    GeofenceAreaEntry(longitude = 12.5685753, latitude = 43.9835427, name = "Ospedaletto")
                )

                for(area in geofenceAreas) {
                    db.GeofenceAreaDao().insertGeofenceArea(area)
                }

                _inserted.value = true
            } else {
                _inserted.value = false
            }
        }
    }

    fun getGeofenceAreas() {
        areas = db.GeofenceAreaDao().getAllGeofenceArea()
    }

    fun observeGeofenceAreasLiveData(): LiveData<List<GeofenceAreaEntry>> {
        return areas
    }
}