package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserWalkActivityDao {
    @Query("SELECT id FROM UserWalkActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getWalkActivitiesByUserId(userId: Long): List<UserWalkActivityEntity>

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getWalkActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserWalkActivityEntity>

    @Insert()
    suspend fun insertWalkActivity(userWalkActivity: UserWalkActivityEntity)

    @Query("UPDATE UserWalkActivity SET exit_time=:exitTime, step_number=:stepsNumber, height_difference=:heightDifference WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long, stepsNumber: Long, heightDifference: Float)
}