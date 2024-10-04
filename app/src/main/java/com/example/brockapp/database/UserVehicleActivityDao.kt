package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserVehicleActivityDao {
    @Query("SELECT id FROM UserVehicleActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id=:userId ORDER BY TIMESTAMP")
    suspend fun getVehicleActivitiesByUserId(userId: Long): List<UserVehicleActivityEntity>

    @Insert()
    suspend fun insertVehicleActivity(userVehicleActivity: UserVehicleActivityEntity)

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getVehicleActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserVehicleActivityEntity>

    @Query("UPDATE UserVehicleActivity SET exit_time=:exitTime, distance_travelled=:distanceTraveled WHERE id=:id")
    suspend fun updateExitTimeAndDistance(id: Long, exitTime: Long, distanceTraveled: Double)
}