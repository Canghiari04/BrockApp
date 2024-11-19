package com.example.brockapp.data

data class GeofenceTransition(
    val timestamps: List<Pair<String, String>>,
    val nameLocation: String,
    val longitude: Double,
    val latitude: Double,
    val averageTime: String,
    val count: Int
)