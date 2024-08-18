package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserStillActivityDao {
    @Insert()
    suspend fun insertStillActivity(userStillActivity: UserStillActivityEntity)

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND transition_type=1 AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getEndingStillActivitiesByUserIdAndPeriod(
        userId: Long,
        startTime: String,
        endTime: String
    ): List<UserStillActivityEntity>

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getStillActivitiesByUserIdAndPeriod(
        userId: Long,
        startTime: String,
        endTime: String
    ): List<UserStillActivityEntity>
}