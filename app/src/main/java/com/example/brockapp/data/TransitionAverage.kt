package com.example.brockapp.data

data class TransitionAverage(
    val nameLocation: String,
    val latitude: Double,
    val longitude: Double,
    val averageTime: java.time.Duration,
    val count: Long
)