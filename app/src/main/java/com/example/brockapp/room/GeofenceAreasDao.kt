package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface GeofenceAreaDao {
    @Query("SELECT COUNT(*) FROM GeofenceArea")
    suspend fun countAllGeofenceAreas(): Int

    @Query("SELECT COUNT(*)>0 FROM GeofenceArea WHERE name=:locationName")
    suspend fun countGeofenceAreaName(locationName: String): Boolean

    @Query("SELECT * FROM GeofenceArea WHERE user_id=:id AND longitude>0 AND latitude>0")
    suspend fun getAllGeofenceAreasByUserId(id: Long): List<GeofenceAreaEntity>

    @Query("SELECT COUNT(*)>0 FROM GeofenceArea WHERE longitude=:longitude AND latitude=:latitude")
    suspend fun checkIfAreaIsPresent(longitude: Double, latitude: Double): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofenceArea(area: GeofenceAreaEntity)

    @Query("DELETE FROM GeofenceArea WHERE name=:name AND longitude=:longitude AND latitude=:latitude")
    suspend fun deleteGeofenceArea(name: String, longitude: Double, latitude: Double)
}