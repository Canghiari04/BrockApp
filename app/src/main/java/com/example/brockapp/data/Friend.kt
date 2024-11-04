package com.example.brockapp.data

import com.example.brockapp.room.UsersRunActivityEntity
import com.example.brockapp.room.UsersWalkActivityEntity
import com.example.brockapp.room.UsersStillActivityEntity
import com.example.brockapp.room.GeofenceTransitionsEntity
import com.example.brockapp.room.UsersVehicleActivityEntity

data class Friend (
    val username: String,
    val typeActivity: String,
    val country: String,
    val city: String,
    var walkActivities: MutableList<UsersWalkActivityEntity> = mutableListOf(),
    var stillActivities: MutableList<UsersStillActivityEntity> = mutableListOf(),
    var runActivities: MutableList<UsersRunActivityEntity> = mutableListOf(),
    var vehicleActivities: MutableList<UsersVehicleActivityEntity> = mutableListOf(),
    var geofenceTransitions: MutableList<GeofenceTransitionsEntity> = mutableListOf()
)