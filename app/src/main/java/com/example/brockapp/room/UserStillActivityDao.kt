package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserStillActivityDao {
    @Query("SELECT id FROM UserStillActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long?

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getStillActivitiesByUserId(userId: Long): List<UserStillActivityEntity>

    @Query("SELECT * FROM UserStillActivity WHERE user_id=:userId AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getStillActivitiesByUserIdAndPeriod(userId: Long, startTime: String, endTime: String): List<UserStillActivityEntity>

    @Insert()
    suspend fun insertStillActivity(userStillActivity: UserStillActivityEntity)

    @Query("UPDATE UserStillActivity SET exit_time=:exitTime WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long)
}
