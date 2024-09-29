package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoDao {
    @Insert()
    suspend fun insertMemo(memo: MemoEntity)
    
    @Query("SELECT * FROM Memo WHERE user_id=:userId AND date=:date ORDER BY timestamp")
    suspend fun getMemoFromUsernameAndPeriod(userId: Long, date: String): List<MemoEntity>

    @Delete()
    suspend fun deleteMemo(memo: MemoEntity)
}