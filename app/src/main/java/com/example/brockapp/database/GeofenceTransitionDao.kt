package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface GeofenceTransitionDao {
    @Insert()
    suspend fun insertGeofenceTransition(transition: GeofenceTransitionEntity)

    @Query("SELECT id FROM GeofenceTransition ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM GeofenceTransition WHERE exit_time!=0")
    suspend fun getGeofenceTransition(): List<GeofenceTransitionEntity>

    @Query("UPDATE GeofenceTransition SET exit_time=:exitTime WHERE id=:id")
    suspend fun updateExitTime(id: Long, exitTime: Long)
}