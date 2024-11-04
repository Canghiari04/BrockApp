package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UsersStillActivityDao {
    @Query("SELECT id FROM UsersStillActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long

    @Query("SELECT * FROM UsersStillActivity WHERE username=:username AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getStillActivitiesByUsername(username: String): List<UsersStillActivityEntity>

    @Query("SELECT * FROM UsersStillActivity WHERE username=:username AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getStillActivitiesByUsernameAndPeriod(username: String, startTime: String, endTime: String): List<UsersStillActivityEntity>

    @Insert
    suspend fun insertStillActivity(userStillActivity: UsersStillActivityEntity)

    @Insert
    suspend fun insertStillActivities(userStillActivities: List<UsersStillActivityEntity>)

    @Query("UPDATE UsersStillActivity SET exit_time=:exitTime WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long)
}
