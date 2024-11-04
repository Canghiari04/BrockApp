package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface MemosDao {
    @Query("SELECT * FROM Memos WHERE username=:username")
    suspend fun getMemosByUsername(username: String): List<MemosEntity>

    @Query("SELECT * FROM Memos WHERE username=:username AND date=:date ORDER BY time_stamp")
    suspend fun getMemosByUsernameAndPeriod(username: String, date: String): List<MemosEntity>

    @Insert
    suspend fun insertMemo(memo: MemosEntity)

    @Insert
    suspend fun insertMemos(memos: List<MemosEntity>)

    @Query("UPDATE Memos SET title=:title, description=:description, activity_type=:activityType WHERE id=:id")
    suspend fun updateMemo(id: Long, title: String, description: String, activityType: String)

    @Delete
    suspend fun deleteMemo(memo: MemosEntity)
}