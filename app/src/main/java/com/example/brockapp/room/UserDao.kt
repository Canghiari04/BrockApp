package com.example.brockapp.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserDao {
    @Query("SELECT COUNT(*)>0 FROM User WHERE username=:username AND password=:password")
    suspend fun checkIfUserIsPresent(username: String, password: String): Boolean

    @Query("SELECT id FROM User WHERE username=:username AND password=:password")
    suspend fun getIdFromUsernameAndPassword(username: String, password: String): Long

    @Query("SELECT * FROM User WHERE username=:username AND password=:password")
    suspend fun getUserFromUsernameAndPassword(username: String, password: String): UserEntity?

    @Insert()
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM User WHERE username=:username AND password=:password")
    suspend fun deleteUserByUsernameAndPassword(username: String, password: String)
}