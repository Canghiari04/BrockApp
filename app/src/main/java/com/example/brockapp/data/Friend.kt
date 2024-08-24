package com.example.brockapp.data

import com.example.brockapp.activity.StillActivity
import com.example.brockapp.activity.VehicleActivity
import com.example.brockapp.activity.WalkActivity

data class Friend(
    val username : String,
    var walkActivities: MutableList<WalkActivity> = mutableListOf<WalkActivity>(),
    var vehicleActivities : MutableList<VehicleActivity> = mutableListOf<VehicleActivity>(),
    var stillActivities : MutableList<StillActivity> = mutableListOf<StillActivity>()
)