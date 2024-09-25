package com.example.brockapp.data

import kotlin.time.Duration

data class TransitionAverage(
    val nameLocation: String,
    val latitude: Double,
    val longitude: Double,
    val averageTime: Duration,
    val count: Long
)