package com.example.brockapp.room

import com.example.brockapp.*

import androidx.room.Room
import androidx.room.Database
import android.content.Context
import androidx.room.RoomDatabase

@Database(
    entities = [UsersEntity::class, UsersVehicleActivityEntity::class, UsersRunActivityEntity::class, UsersStillActivityEntity::class, UsersWalkActivityEntity::class, GeofenceAreasEntity::class, GeofenceTransitionsEntity::class, FriendsEntity::class, MemosEntity::class],
    version = ROOM_DATABASE_VERSION
)
abstract class BrockDB: RoomDatabase() {
    abstract fun UsersDao(): UsersDao

    abstract fun UsersVehicleActivityDao(): UsersVehicleActivityDao

    abstract fun UsersStillActivityDao(): UsersStillActivityDao

    abstract fun UsersRunActivityDao(): UsersRunActivityDao

    abstract fun UsersWalkActivityDao(): UsersWalkActivityDao

    abstract fun MemosDao(): MemosDao

    abstract fun GeofenceAreasDao(): GeofenceAreasDao

    abstract fun GeofenceTransitionsDao(): GeofenceTransitionsDao

    abstract fun FriendsDao(): FriendsDao

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
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE as BrockDB
        }
    }
}