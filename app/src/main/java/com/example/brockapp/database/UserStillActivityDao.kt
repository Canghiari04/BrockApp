package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserStillActivityDao {
    @Query("SELECT id FROM UserStillActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId ORDER BY TIMESTAMP")
    suspend fun getStillActivitiesByUserId(userId: Long): List<UserStillActivityEntity>

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    suspend fun getStillActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserStillActivityEntity>

    @Insert()
    suspend fun insertStillActivity(userStillActivity: UserStillActivityEntity)

    @Query("UPDATE UserStillActivity SET exit_time=:exitTime WHERE id=:id")
    suspend fun updateExitTime(id: Long, exitTime: Long)
}
