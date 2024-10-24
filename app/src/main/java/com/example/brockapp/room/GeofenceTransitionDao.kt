package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface GeofenceTransitionDao {
    @Query("SELECT id FROM GeofenceTransition ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM GeofenceTransition WHERE user_id=:userId AND exit_time>arrival_time")
    suspend fun getAllGeofenceTransitionByUserId(userId: Long): List<GeofenceTransitionEntity>

    @Insert()
    suspend fun insertGeofenceTransition(transition: GeofenceTransitionEntity)

    @Query("UPDATE GeofenceTransition SET exit_time=:exitTime WHERE id=:id")
    suspend fun updateExitTime(id: Long, exitTime: Long)
}