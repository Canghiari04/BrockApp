package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UsersRunActivityDao {
    @Query("SELECT id FROM UsersRunActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long

    @Query("SELECT * FROM UsersRunActivity WHERE username=:username AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getRunActivitiesByUsername(username: String): List<UsersRunActivityEntity>

    @Query("SELECT * FROM UsersRunActivity WHERE username=:username AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getRunActivitiesByUsernameAndPeriod(username: String, startTime: String, endTime: String): List<UsersRunActivityEntity>

    @Insert
    suspend fun insertRunActivity(userRunActivity: UsersRunActivityEntity)

    @Insert
    suspend fun insertRunActivities(userRunActivities: List<UsersRunActivityEntity>)

    @Query("UPDATE UsersRunActivity SET exit_time=:exitTime, distance_done=:distanceDone, height_difference=:heightDifference WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long, distanceDone: Double, heightDifference: Float)

    @Query("DELETE FROM UsersRunActivity WHERE id=:id")
    suspend fun deleteRunActivity(id: Long)
}