package com.example.brockapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "GeofenceArea")
data class GeofenceAreaEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long?,
    val longitude: Double,
    val latitude: Double,
    val name: String
)