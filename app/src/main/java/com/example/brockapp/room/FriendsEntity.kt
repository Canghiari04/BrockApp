package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "Friends",
    foreignKeys = [
        ForeignKey(
            entity = UsersEntity::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("username"),
            onDelete = ForeignKey.CASCADE)
    ]
)
data class FriendsEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    @ColumnInfo(name = "username_friend") val usernameFriend: String
)