package com.example.brockapp.data

import com.example.brockapp.room.UserRunActivityEntity
import com.example.brockapp.room.UserWalkActivityEntity
import com.example.brockapp.room.UserStillActivityEntity
import com.example.brockapp.room.GeofenceTransitionEntity
import com.example.brockapp.room.UserVehicleActivityEntity

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