package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface GeofenceTransitionsDao {
    @Query("SELECT id FROM GeofenceTransitions ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long

    @Query("SELECT * FROM GeofenceTransitions ORDER BY id DESC LIMIT 1")
    suspend fun getLastTransition(): GeofenceTransitionsEntity

    @Query("SELECT * FROM GeofenceTransitions WHERE username=:username AND exit_time>arrival_time")
    suspend fun getAllGeofenceTransitionsByUsername(username: String): List<GeofenceTransitionsEntity>

    @Insert
    suspend fun insertGeofenceTransition(transition: GeofenceTransitionsEntity)

    @Insert
    suspend fun insertGeofenceTransitions(transitions: List<GeofenceTransitionsEntity>)

    @Query("UPDATE GeofenceTransitions SET exit_time=:exitTime WHERE id=:id")
    suspend fun updateExitTime(id: Long, exitTime: Long)
}