package com.example.brockapp.interfaces

import android.location.Address

interface ReverseGeocoding {
    fun getAddress(name: String?, latitude: Double, longitude: Double): Address

    suspend fun getGeofenceName(latitude: Double, longitude: Double): String?
}