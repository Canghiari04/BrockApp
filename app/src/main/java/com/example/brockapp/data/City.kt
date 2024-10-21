package com.example.brockapp.data

data class CityResponse (
    val data: List<City>
)

data class City (
    val id: Int,
    val name: String,
    val country: String,
    val region: String
)