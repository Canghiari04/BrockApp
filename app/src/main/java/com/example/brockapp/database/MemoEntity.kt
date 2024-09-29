package com.example.brockapp.database

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.amazonaws.transform.TimestampFormat
import com.google.firebase.Timestamp
import java.util.Date

@Entity(
    tableName = "Memo",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class MemoEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long?,
    val title: String,
    val description: String,
    @ColumnInfo(name = "activity_type") val activityType: String,
    val date: String,
    val timestamp: String
)