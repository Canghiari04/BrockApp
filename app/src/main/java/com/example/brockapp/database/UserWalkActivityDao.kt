package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserWalkActivityDao {
    @Insert()
    suspend fun insertWalkActivity(userWalkActivity: UserWalkActivityEntity)

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId ORDER BY TIMESTAMP")
    suspend fun getWalkActivitiesByUserId(userId: Long): List<UserWalkActivityEntity>

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId AND transition_type=1 AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getEndingWalkActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserWalkActivityEntity>

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getWalkActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserWalkActivityEntity>

    @Query("SELECT COUNT(*) FROM UserWalkActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime AND transition_type=1")
    suspend fun getWalkActivitiesCountByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): Int
}