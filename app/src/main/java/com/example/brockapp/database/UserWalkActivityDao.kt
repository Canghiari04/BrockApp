package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserWalkActivityDao {
    @Insert()
    suspend fun insertWalkActivity(userWalkActivity: UserWalkActivityEntity)

    @Query("SELECT * FROM UserWalkActivity WHERE user_id=:userId AND transition_type=1 AND timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp")
    suspend fun getWalkActivitiesByUserIdAndDay(userId: Long, startOfDay: String, endOfDay: String): List<UserWalkActivityEntity>
}