package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "GeofenceTransitions",
    foreignKeys = [ForeignKey(
        entity = UsersEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class GeofenceTransitionsEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "name_location") val nameLocation: String,
    val longitude: Double,
    val latitude: Double,
    @ColumnInfo(name = "arrival_time") val arrivalTime: Long,
    @ColumnInfo(name = "exit_time") val exitTime: Long
)