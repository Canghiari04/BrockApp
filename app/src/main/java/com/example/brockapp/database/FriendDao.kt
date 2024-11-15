package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("SELECT followedUsername FROM Friend WHERE user_id = :userId")
    suspend fun getFriendsByUserId(userId: Long): List<String>

    @Query("DELETE FROM Friend WHERE id=:userId AND followedUsername=:username")
    suspend fun deleteFriend(userId: Long, username: String)
}