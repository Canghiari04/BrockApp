package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UsersVehicleActivityDao {
    @Query("SELECT id FROM UsersVehicleActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long

    @Query("SELECT * FROM UsersVehicleActivity WHERE username=:username AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getVehicleActivitiesByUsername(username: String): List<UsersVehicleActivityEntity>

    @Query("SELECT * FROM UsersVehicleActivity WHERE username=:username AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getVehicleActivitiesByUsernameAndPeriod(username: String, startTime: String, endTime: String): List<UsersVehicleActivityEntity>

    @Insert
    suspend fun insertVehicleActivity(userVehicleActivity: UsersVehicleActivityEntity)

    @Insert
    suspend fun insertVehicleActivities(userVehicleActivities: List<UsersVehicleActivityEntity>)

    @Query("UPDATE UsersVehicleActivity SET exit_time=:exitTime, distance_travelled=:distanceTravelled WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long, distanceTravelled: Double)
}