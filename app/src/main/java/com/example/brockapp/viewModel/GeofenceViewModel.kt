package com.example.brockapp.viewmodel

import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.room.GeofenceAreasEntity
import com.example.brockapp.room.GeofenceTransitionsEntity

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class GeofenceViewModel(private val db: BrockDB): ViewModel() {
    private val _geofenceTransitions = MutableLiveData<List<GeofenceTransitionsEntity>>()
    val geofenceTransitions: LiveData<List<GeofenceTransitionsEntity>> get() = _geofenceTransitions

    private val _isAlreadyIn = MutableLiveData<Boolean>()
    val isAlreadyIn: LiveData<Boolean> get() = _isAlreadyIn

    private val _staticAreas = MutableLiveData<List<GeofenceAreasEntity>>()
    val staticAreas: LiveData<List<GeofenceAreasEntity>> get() = _staticAreas

    private val _dynamicAreas = MutableLiveData<List<GeofenceAreasEntity>>()
    val dynamicAreas: LiveData<List<GeofenceAreasEntity>> get() = _dynamicAreas

    fun getGeofenceTransitions(startOfPeriod: String, endOfPeriod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val transitions = db.GeofenceTransitionsDao().getGeofenceTransitionsByUsernameAndPeriod(
                MyUser.username,
                startOfPeriod,
                endOfPeriod
            ).filter { it.nameLocation.isNotBlank() }

            _geofenceTransitions.postValue(transitions)
        }
    }

    fun checkGeofenceAreaAlreadyIn(area: GeofenceAreasEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = db.GeofenceAreasDao().countGeofenceArea(area.name, area.latitude, area.longitude)
            _isAlreadyIn.postValue(item)
        }
    }

    fun insertGeofenceArea(area: GeofenceAreasEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.GeofenceAreasDao().insertGeofenceArea(area)
            fetchDynamicGeofenceAreas()
        }
    }

    // Difference between the fetch methods is that this one is used to define the area already inside the database
    fun fetchStaticGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.GeofenceAreasDao().getGeofenceAreasByUsername(MyUser.username)
            _staticAreas.postValue(items)
        }
    }

    // Second one used to define new area added from the MapFragment by the user
    private fun fetchDynamicGeofenceAreas() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.GeofenceAreasDao().getGeofenceAreasByUsername(MyUser.username)
            _dynamicAreas.postValue(items)
        }
    }

    fun deleteGeofenceArea(longitude: Double, latitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            db.GeofenceAreasDao().deleteGeofenceArea(longitude, latitude)
            fetchDynamicGeofenceAreas()
        }
    }
}