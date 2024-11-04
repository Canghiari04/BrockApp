package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface FriendDao {
    @Query("SELECT username FROM Friend WHERE user_id = :userId")
    suspend fun getFriendsByUserId(userId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendsEntity)

    @Query("DELETE FROM Friend WHERE user_id=:userId AND username=:username")
    suspend fun deleteFriend(userId: Long, username: String): Int
}