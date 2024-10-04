package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserWalkActivityDao {
    @Query("SELECT id FROM UserWalkActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId ORDER BY TIMESTAMP")
    suspend fun getWalkActivitiesByUserId(userId: Long): List<UserWalkActivityEntity>

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getWalkActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserWalkActivityEntity>

    @Insert()
    suspend fun insertWalkActivity(userWalkActivity: UserWalkActivityEntity)

    @Query("UPDATE UserWalkActivity SET exit_time=:exitTime, step_number=:stepsNumber WHERE id=:id")
    suspend fun updateExitTimeAndSteps(id: Long, exitTime: Long, stepsNumber: Long)
}