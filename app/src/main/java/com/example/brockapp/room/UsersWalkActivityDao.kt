package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UsersWalkActivityDao {
    @Query("SELECT id FROM UsersWalkActivity ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Long

    @Query("SELECT * FROM UsersWalkActivity WHERE username=:username AND exit_time>arrival_time ORDER BY time_stamp")
    suspend fun getWalkActivitiesByUsername(username: String): List<UsersWalkActivityEntity>

    @Query("SELECT * FROM UsersWalkActivity WHERE username=:username AND exit_time>arrival_time AND time_stamp BETWEEN :startTime AND :endTime ORDER BY time_stamp")
    suspend fun getWalkActivitiesByUsernameAndPeriod(username: String, startTime: String, endTime: String): List<UsersWalkActivityEntity>

    @Insert
    suspend fun insertWalkActivity(userWalkActivity: UsersWalkActivityEntity)

    @Insert
    suspend fun insertWalkActivities(userWalkActivities: List<UsersWalkActivityEntity>)

    @Query("UPDATE UsersWalkActivity SET exit_time=:exitTime, steps_number=:stepsNumber, height_difference=:heightDifference WHERE id=:id")
    suspend fun updateLastRecord(id: Long, exitTime: Long, stepsNumber: Long, heightDifference: Float)
}