package com.example.brockapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Friend")
data class FriendEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val followedUsername: String
)