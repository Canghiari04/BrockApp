package com.example.brockapp.data

import com.example.brockapp.database.UserWalkActivityEntity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity

data class Friend (
    val username: String,
    var walkActivities: MutableList<UserWalkActivityEntity> = mutableListOf(),
    var stillActivities: MutableList<UserStillActivityEntity> = mutableListOf(),
    var vehicleActivities: MutableList<UserVehicleActivityEntity> = mutableListOf()
)