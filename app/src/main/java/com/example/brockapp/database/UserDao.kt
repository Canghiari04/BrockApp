package com.example.brockapp.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface UserDao {
    @Insert()
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT COUNT(*)>0 FROM User WHERE username=:username AND password=:password")
    suspend fun checkIfUserIsPresent(username: String, password: String): Boolean

    @Query("SELECT id FROM User WHERE username=:username AND password=:password")
    suspend fun getIdFromUsernameAndPassword(username: String, password: String): Long

    @Query("SELECT * FROM User WHERE username=:username AND password=:password")
    suspend fun getUserFromUsernameAndPassword(username: String, password: String): UserEntity?

    @Query("SELECT sharing_flag FROM User WHERE username=:username AND password=:password")
    suspend fun getSharingFlagFromUsernameAndPassword(username: String, password: String): Boolean

    @Query("UPDATE User SET sharing_flag=1 WHERE username=:username AND password=:password")
    suspend fun changeFlag(username: String, password: String)

    @Query("DELETE FROM User WHERE id=:id")
    suspend fun deleteUserById(id: Long)
}