package com.example.brockapp.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "Users",
    primaryKeys = ["username"]
)
data class UsersEntity (
    val username: String,
    val password: String,
    @ColumnInfo("type_activity") val typeActivity: String,
    val country: String,
    val city: String
)