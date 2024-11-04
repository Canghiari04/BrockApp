package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UsersDao {
    @Query("SELECT COUNT(*)>0 FROM Users WHERE username=:username AND password=:password")
    suspend fun checkIfUserIsPresent(username: String, password: String): Boolean

    @Query("SELECT * FROM Users WHERE username=:username AND password=:password")
    suspend fun getUserFromUsernameAndPassword(username: String, password: String): UsersEntity

    @Insert()
    suspend fun insertUser(user: UsersEntity)

    @Query("DELETE FROM Users WHERE username=:username AND password=:password")
    suspend fun deleteUser(username: String, password: String)
}