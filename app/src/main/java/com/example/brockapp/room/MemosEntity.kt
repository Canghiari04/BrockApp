package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "Memos",
    foreignKeys = [ForeignKey(
        entity = UsersEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class MemoEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    val title: String,
    val description: String,
    @ColumnInfo(name = "activity_type") val activityType: String,
    val date: String,
    @ColumnInfo(name = "time_stamp") val timestamp: String
)