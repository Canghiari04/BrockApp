package com.example.brockapp.database

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "UserWalkActivity",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserWalkActivityEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    val timestamp: String,
    @ColumnInfo(name = "arrival_time") val arrivalTime: Long,
    @ColumnInfo(name = "exit_time") val exitTime: Long,
    @ColumnInfo(name = "step_number") val stepNumber: Long
)