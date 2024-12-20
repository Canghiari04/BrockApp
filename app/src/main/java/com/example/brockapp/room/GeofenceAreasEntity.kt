package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "GeofenceAreas",
    foreignKeys = [
        ForeignKey(
            entity = UsersEntity::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("username"),
            onDelete = ForeignKey.CASCADE)
    ]
)
data class GeofenceAreasEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val longitude: Double,
    val latitude: Double,
    val name: String
)