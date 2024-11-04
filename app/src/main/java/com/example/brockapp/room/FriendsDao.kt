package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface FriendsDao {
    @Query("SELECT username_friend FROM Friends WHERE username=:username")
    suspend fun getUsernamesFriendsByUsername(username: String): List<String>

    @Query("SELECT * FROM Friends WHERE username=:username")
    suspend fun getFriendsByUsername(username: String): List<FriendsEntity>

    @Insert
    suspend fun insertFriend(friend: FriendsEntity)

    @Insert
    suspend fun insertFriends(friends: List<FriendsEntity>)

    @Query("DELETE FROM Friends WHERE username=:username AND username_friend=:usernameFriend")
    suspend fun deleteFriend(username: String, usernameFriend: String): Int
}