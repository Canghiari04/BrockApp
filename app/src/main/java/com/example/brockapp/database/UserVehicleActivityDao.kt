package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserVehicleActivityDao {
    @Insert()
    suspend fun insertVehicleActivity(userVehicleActivity: UserVehicleActivityEntity)

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id=:userId AND transition_type=1 AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getVehicleActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserVehicleActivityEntity>
}