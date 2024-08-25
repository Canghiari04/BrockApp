package com.example.brockapp.data

import com.example.brockapp.activity.StillActivity
import com.example.brockapp.activity.VehicleActivity
import com.example.brockapp.activity.WalkActivity
import com.example.brockapp.database.UserStillActivityEntity
import com.example.brockapp.database.UserVehicleActivityEntity
import com.example.brockapp.database.UserWalkActivityEntity

data class Friend(
    val username : String,
    var walkActivities: MutableList<UserWalkActivityEntity> = mutableListOf<UserWalkActivityEntity>(),
    var vehicleActivities : MutableList<UserVehicleActivityEntity> = mutableListOf<UserVehicleActivityEntity>(),
    var stillActivities : MutableList<UserStillActivityEntity> = mutableListOf<UserStillActivityEntity>()
)