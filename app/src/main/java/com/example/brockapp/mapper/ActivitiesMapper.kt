package com.example.brockapp.mapper

data class UserWalkActivityMapper(
    val id: Long,
    val userId: Long,
    val transitionType: Int,
    val timestamp: String,
    val stepNumber: Long
)

data class UserVehicleActivityMapper(
    val id: Long,
    val userId: Long,
    val transitionType: Int,
    val timestamp: String,
    val distanceTravelled: Double
)

data class UserStillActivityMapper(
    val id: Long,
    val userId: Long,
    val transitionType: Int,
    val timestamp: String
)

