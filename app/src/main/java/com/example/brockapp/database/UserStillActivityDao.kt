package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserStillActivityDao {
    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId ORDER BY TIMESTAMP")
    suspend fun getStillActivitiesByUserId(userId: Long): List<UserStillActivityEntity>

    @Insert()
    suspend fun insertStillActivity(userStillActivity: UserStillActivityEntity)

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getStillActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserStillActivityEntity>

    @Query("SELECT COUNT(*) FROM UserStillActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime AND transition_type=1")
    suspend fun getStillActivitiesCountByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): Int
}