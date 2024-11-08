package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface GeofenceAreasDao {
    @Query("SELECT COUNT(*)>0 FROM GeofenceAreas WHERE name=:name AND latitude=:latitude AND longitude=:longitude")
    suspend fun countGeofenceArea(name: String, latitude: Double, longitude: Double): Boolean

    @Query("SELECT COUNT(*) FROM GeofenceAreas")
    suspend fun countAllGeofenceAreas(): Int

    @Query("SELECT * FROM GeofenceAreas WHERE username=:username AND longitude>0 AND latitude>0")
    suspend fun getGeofenceAreasByUsername(username: String): List<GeofenceAreasEntity>

    @Query("SELECT COUNT(*)>0 FROM GeofenceAreas WHERE longitude=:longitude AND latitude=:latitude")
    suspend fun checkIfAreaIsPresent(longitude: Double, latitude: Double): Boolean

    @Insert
    suspend fun insertGeofenceArea(area: GeofenceAreasEntity)

    @Insert
    suspend fun insertGeofenceAreas(areas: List<GeofenceAreasEntity>)

    @Query("DELETE FROM GeofenceAreas WHERE longitude=:longitude AND latitude=:latitude")
    suspend fun deleteGeofenceArea(longitude: Double, latitude: Double)
}