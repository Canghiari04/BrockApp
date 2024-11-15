package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy

@Dao
interface GeofenceAreaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGeofenceArea(area: GeofenceAreaEntry)

    @Query("SELECT * FROM GeofenceArea")
    fun getAllGeofenceAreas(): List<GeofenceAreaEntry>

    @Query("SELECT * FROM GeofenceArea")
    fun getAllLiveGeofenceAreas(): LiveData<List<GeofenceAreaEntry>>

    @Query("SELECT COUNT(*) FROM GeofenceArea")
    suspend fun countAllGeofenceAreas(): Int

    @Query("SELECT COUNT(*)>0 FROM GeofenceArea WHERE longitude=:longitude AND latitude=:latitude")
    suspend fun checkIfAreaIsPresent(longitude: Double, latitude: Double): Boolean

    @Query("DELETE FROM GeofenceArea WHERE name=:name AND longitude=:longitude AND latitude=:latitude")
    suspend fun deleteGeofenceArea(name: String?, longitude: Double, latitude: Double)
}