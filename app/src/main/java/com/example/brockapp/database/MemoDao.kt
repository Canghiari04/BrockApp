package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert

@Dao
interface MemoDao {
    @Query("SELECT * FROM Memo WHERE user_id=:userId AND date=:date ORDER BY timestamp")
    suspend fun getMemoFromUsernameAndPeriod(userId: Long, date: String): List<MemoEntity>

    @Insert()
    suspend fun insertMemo(memo: MemoEntity)

    @Delete()
    suspend fun deleteMemo(memo: MemoEntity)
}