package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert()
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT COUNT(*)>0 FROM User WHERE username=:username AND password=:password")
    suspend fun checkIfUserIsPresent(username: String, password: String): Boolean

    @Query("SELECT id FROM User WHERE username=:username AND password=:password")
    suspend fun getIdFromUsernameAndPassword(username: String, password: String): Long

    @Query("SELECT sharing_flag FROM User WHERE username=:username AND password=:password")
    suspend fun getSharingFlagFromUsernameAndPassword(username: String, password: String): Boolean

    @Query("DELETE FROM User WHERE id=:id")
    suspend fun deleteUserById(id: Long)
}