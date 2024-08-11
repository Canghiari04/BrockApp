package com.example.brockapp.data

data class UserActivity(
    val activityId: Long,
    val userId: Long?,
    val timestamp: String?,
    val transitionType : Int,
    val type: String,
    val info: String
)