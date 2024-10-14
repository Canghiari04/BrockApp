package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserVehicleActivityDao {
    @Query("SELECT id FROM UserVehicleActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id=:userId AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getVehicleActivitiesByUserId(userId: Long): List<UserVehicleActivityEntity>

    @Query("SELECT * FROM UserVehicleActivity WHERE user_id=:userId AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getVehicleActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserVehicleActivityEntity>

    @Insert()
    suspend fun insertVehicleActivity(userVehicleActivity: UserVehicleActivityEntity)

    @Query("UPDATE UserVehicleActivity SET exit_time=:exitTime, distance_travelled=:distanceTraveled WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long, distanceTraveled: Double)
}