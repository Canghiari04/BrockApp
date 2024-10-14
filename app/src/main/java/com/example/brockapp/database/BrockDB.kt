package com.example.brockapp.database

import com.example.brockapp.ROOM_DATABASE_VERSION

import androidx.room.Room
import androidx.room.Database
import android.content.Context
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, UserVehicleActivityEntity::class, UserRunActivityEntity::class, UserStillActivityEntity::class, UserWalkActivityEntity::class, GeofenceAreaEntity::class, GeofenceTransitionEntity::class, FriendEntity::class, MemoEntity::class],
    version = ROOM_DATABASE_VERSION
)
abstract class BrockDB: RoomDatabase() {
    abstract fun UserDao(): UserDao
    abstract fun UserVehicleActivityDao(): UserVehicleActivityDao
    abstract fun UserRunActivityDao(): UserRunActivityDao
    abstract fun UserStillActivityDao(): UserStillActivityDao
    abstract fun UserWalkActivityDao(): UserWalkActivityDao
    abstract fun GeofenceAreaDao(): GeofenceAreaDao
    abstract fun GeofenceTransitionDao(): GeofenceTransitionDao
    abstract fun FriendDao(): FriendDao
    abstract fun MemoDao(): MemoDao

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