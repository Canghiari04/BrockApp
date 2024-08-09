package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserStillActivityDao {
    @Insert()
    suspend fun insertStillActivity(userStillActivity: UserStillActivityEntity)

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND transition_type=1 AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getStillActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserStillActivityEntity>
}