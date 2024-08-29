package com.example.brockapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.brockapp.ROOM_DATABASE_VERSION

@Database(
    entities = [UserEntity::class, UserStillActivityEntity::class, UserVehicleActivityEntity::class, UserWalkActivityEntity::class, GeofenceAreaEntry::class, FriendEntity::class],
    version = ROOM_DATABASE_VERSION
)
abstract class BrockDB: RoomDatabase() {
    abstract fun UserDao(): UserDao
    abstract fun UserStillActivityDao(): UserStillActivityDao
    abstract fun UserVehicleActivityDao(): UserVehicleActivityDao
    abstract fun UserWalkActivityDao(): UserWalkActivityDao
    abstract fun GeofenceAreaDao(): GeofenceAreaDao
    abstract fun FriendDao(): FriendDao


    companion object {
        @Volatile
        var INSTANCE: BrockDB? = null

        @Synchronized
        fun getInstance(context: Context): BrockDB {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    BrockDB::class.java,
                    "brock.db"
                ).fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE as BrockDB
        }
    }
}