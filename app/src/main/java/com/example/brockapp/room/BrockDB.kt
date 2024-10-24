package com.example.brockapp.room

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.ROOM_DATABASE_VERSION

import androidx.room.Room
import androidx.room.Database
import android.content.Context
import kotlinx.coroutines.launch
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserEntity::class, UserVehicleActivityEntity::class, UserRunActivityEntity::class, UserStillActivityEntity::class, UserWalkActivityEntity::class, GeofenceAreaEntity::class, GeofenceTransitionEntity::class, FriendEntity::class, MemoEntity::class],
    version = ROOM_DATABASE_VERSION
)
abstract class BrockDB: RoomDatabase() {
    abstract fun UserDao(): UserDao

    abstract fun UserVehicleActivityDao(): UserVehicleActivityDao

    abstract fun UserStillActivityDao(): UserStillActivityDao

    abstract fun UserRunActivityDao(): UserRunActivityDao

    abstract fun UserWalkActivityDao(): UserWalkActivityDao

    abstract fun MemoDao(): MemoDao

    abstract fun GeofenceAreaDao(): GeofenceAreaDao

    abstract fun GeofenceTransitionDao(): GeofenceTransitionDao

    abstract fun FriendDao(): FriendDao

    companion object {
        private val areas = listOf(
            GeofenceAreaEntity(userId = MyUser.id, longitude = 11.326957, latitude = 44.476543, name = "Villa Ghigi"),
            GeofenceAreaEntity(userId = MyUser.id, longitude = 11.346302, latitude = 44.502505, name = "Parco della Montagnola")
        )

        @Volatile
        var INSTANCE: BrockDB? = null

        @Synchronized
        fun getInstance(context: Context): BrockDB {
            if (INSTANCE == null) {
                INSTANCE ?: prepopulateDatabase(context).also { INSTANCE = it }
            }

            return INSTANCE as BrockDB
        }

        // Function used the first time when the database is created, so for the first sign in
        private fun prepopulateDatabase(context: Context) =
            Room.databaseBuilder(
                    context.applicationContext,
                    BrockDB::class.java,
                    "brock.db"
                ).fallbackToDestructiveMigration()
                .addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                // Instance of db
                                val instance = getInstance(context)

                                for (area in areas) {
                                    instance.GeofenceAreaDao().insertGeofenceArea(area)
                                }
                            }
                        }
                    }
                )
                .build()
    }
}