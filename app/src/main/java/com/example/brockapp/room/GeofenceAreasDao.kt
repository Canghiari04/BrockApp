package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface GeofenceAreasDao {
    @Query("SELECT COUNT(*) FROM GeofenceAreas")
    suspend fun countAllGeofenceAreas(): Int

    @Query("SELECT COUNT(*)>0 FROM GeofenceAreas WHERE name=:locationName")
    suspend fun countGeofenceAreaName(locationName: String): Boolean

    @Query("SELECT * FROM GeofenceAreas WHERE username=:username AND longitude>0 AND latitude>0")
    suspend fun getAllGeofenceAreasByUsername(username: String): List<GeofenceAreasEntity>

    @Query("SELECT COUNT(*)>0 FROM GeofenceAreas WHERE longitude=:longitude AND latitude=:latitude")
    suspend fun checkIfAreaIsPresent(longitude: Double, latitude: Double): Boolean

    @Insert
    suspend fun insertGeofenceArea(area: GeofenceAreasEntity)

    @Insert
    suspend fun insertGeofenceAreas(areas: List<GeofenceAreasEntity>)

    @Query("DELETE FROM GeofenceAreas WHERE longitude=:longitude AND latitude=:latitude")
    suspend fun deleteGeofenceArea(longitude: Double, latitude: Double)
}