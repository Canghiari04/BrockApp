package com.example.brockapp.data

import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.GeofenceTransitionEntity
import com.example.brockapp.database.UserRunActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

data class Friend (
    val username: String,
    val typeActivity: String,
    val country: String,
    val city: String,
    var walkActivities: MutableList<UserWalkActivityEntity> = mutableListOf(),
    var stillActivities: MutableList<UserStillActivityEntity> = mutableListOf(),
    var runActivities: MutableList<UserRunActivityEntity> = mutableListOf(),
    var vehicleActivities: MutableList<UserVehicleActivityEntity> = mutableListOf(),
    var geofenceTransitions: MutableList<GeofenceTransitionEntity> = mutableListOf()
)