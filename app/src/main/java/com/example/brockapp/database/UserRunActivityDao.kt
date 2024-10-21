package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserRunActivityDao {
    @Query("SELECT id FROM UserRunActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserRunActivity WHERE user_id=:userId AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getRunActivitiesByUserId(userId: Long): List<UserRunActivityEntity>

    @Query("SELECT * FROM UserRunActivity WHERE user_id=:userId AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getRunActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserRunActivityEntity>

    @Insert()
    suspend fun insertRunActivity(userRunActivity: UserRunActivityEntity)

    @Query("UPDATE UserRunActivity SET exit_time=:exitTime, distance_done=:distanceDone, height_difference=:heightDifference WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long, distanceDone: Double, heightDifference: Float)

}