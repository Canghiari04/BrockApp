package com.example.brockapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object UserActivity {
        // Table contents are grouped together in an anonymous object.
        object UserActivityEntry : BaseColumns {
            const val TABLE_NAME = "user-activity"
            const val COLUMN_NAME = "name"
            const val COLUMN_ACTIVITY_TYPE = "activity-type"
            const val COLUMN_TRANSITION_TYPE = "transition-type"
            const val COLUMN_TIMESTAMP = "timestamp"
        }
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${UserActivityEntry.TABLE_NAME} (${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                    "${StudentEntry.COLUMN_NAME} TEXT, ${StudentEntry.COLUMN_CLASS} TEXT," +
                    "${StudentEntry.COLUMN_GRADE} TEXT)"
        )
    }
    companion object { const val DATABASE_NAME = "Students.db", const val DATABASE_VERSION = 1 }
}