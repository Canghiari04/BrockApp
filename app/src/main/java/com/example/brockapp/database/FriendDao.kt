package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface FriendDao {
    @Query("SELECT username FROM Friend")
    suspend fun getAllFriends(): List<String>

    @Query("SELECT username FROM Friend WHERE user_id = :userId")
    suspend fun getFriendsByUserId(userId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("DELETE FROM Friend WHERE user_id=:userId AND username=:username")
    suspend fun deleteFriend(userId: Long, username: String): Int
}