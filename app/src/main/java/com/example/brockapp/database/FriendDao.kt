package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FriendDao {

    @Insert()
    suspend fun insertFriend(friend: FriendEntity)

    @Query("SELECT * FROM Friend WHERE userId = :userId")
    suspend fun getFriendsByUserId(userId: Long): List<FriendEntity>
}