package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserVehicleActivityDao {
    @Query("SELECT * FROM UserVehicleActivity WHERE user_id=:userId ORDER BY TIMESTAMP")
    suspend fun getVehicleActivitiesByUserId(userId: Long): List<UserVehicleActivityEntity>

    @Insert()
    suspend fun insertVehicleActivity(userVehicleActivity: UserVehicleActivityEntity)

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id=:userId AND transition_type=1 AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getEndingVehicleActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserVehicleActivityEntity>

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getVehicleActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserVehicleActivityEntity>

    @Query("SELECT COUNT(*) FROM UserVehicleActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime AND transition_type=1")
    suspend fun getVehicleActivitiesCountByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): Int
}