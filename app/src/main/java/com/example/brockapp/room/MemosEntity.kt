package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "Memos",
    foreignKeys = [
        ForeignKey(
            entity = UsersEntity::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("username"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MemosEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "activity_type") val activityType: String,
    val date: String,
    @ColumnInfo(name = "time_stamp") val timestamp: String
)