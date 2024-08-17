package com.example.brockapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GeofenceArea")
data class GeofenceAreaEntry (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val longitude: Double,
    val latitude: Double,
    val name: String
)