package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("SELECT followedUsername FROM Friend WHERE userId = :userId")
    suspend fun getFriendsByUserId(userId: Long): List<String>

    @Query("DELETE FROM Friend WHERE id=:userId AND followedUsername=:username")
    suspend fun deleteFriend(userId: Long, username: String)
}