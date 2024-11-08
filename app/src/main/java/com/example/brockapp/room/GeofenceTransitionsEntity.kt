package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "GeofenceTransitions",
    foreignKeys = [
        ForeignKey(
            entity = UsersEntity::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("username"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GeofenceTransitionsEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    @ColumnInfo(name = "time_stamp") val timestamp: String,
    @ColumnInfo(name = "name_location") val nameLocation: String,
    val longitude: Double,
    val latitude: Double,
    @ColumnInfo(name = "arrival_time") val arrivalTime: Long,
    @ColumnInfo(name = "exit_time") val exitTime: Long
)