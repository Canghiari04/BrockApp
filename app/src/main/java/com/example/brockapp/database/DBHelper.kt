package com.example.brockapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.brockapp.DATABASE_NAME
import com.example.brockapp.DATABASE_VERSION

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


        // Table contents are grouped together in an anonymous object.
    object UserActivityEntry : BaseColumns {
        const val TABLE_NAME = "user-activity"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ACTIVITY_TYPE = "activity-type"
        const val COLUMN_TRANSITION_TYPE = "transition-type"
        const val COLUMN_TIMESTAMP = "timestamp"
    }


    object UserEntry {
        const val TABLE_NAME = "user"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"

    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${UserActivityEntry.TABLE_NAME} (${UserActivityEntry.COLUMN_ID} INTEGER PRIMARY KEY, " +
                    "${UserActivityEntry.COLUMN_NAME} TEXT, ${UserActivityEntry.COLUMN_ACTIVITY_TYPE} TEXT," +
                    "${UserActivityEntry.COLUMN_TRANSITION_TYPE} TEXT, ${UserActivityEntry.COLUMN_TIMESTAMP} LONG)"
        )
        db.execSQL(
            "CREATE TABLE ${UserEntry.TABLE_NAME} (${UserEntry.COLUMN_ID} INTEGER PRIMARY KEY, " +
                    "${UserEntry.COLUMN_USERNAME} TEXT, ${UserEntry.COLUMN_PASSWORD} TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun insertUser(dbHelper: DbHelper, username: String, password: String) : Long? {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbHelper.UserEntry.COLUMN_USERNAME, username)
            put(DbHelper.UserEntry.COLUMN_PASSWORD, password)

            // Inserisci la nuova riga
            val newRowId = db?.insert(DbHelper.UserEntry.TABLE_NAME, null, this)
            return newRowId
        }



    }

    fun insertUserActivity(dbHelper: DbHelper, name: String, activityType: String, transitionType: String, timestamp: Long
    ) : Long?{
        // Ottieni il database in modalità scrittura
        val db = dbHelper.writableDatabase

        // Crea una nuova mappa di valori, dove i nomi delle colonne sono le chiavi
        val values = ContentValues().apply {
            put(DbHelper.UserActivityEntry.COLUMN_NAME, name)
            put(DbHelper.UserActivityEntry.COLUMN_ACTIVITY_TYPE, activityType)
            put(DbHelper.UserActivityEntry.COLUMN_TRANSITION_TYPE, transitionType)
            put(DbHelper.UserActivityEntry.COLUMN_TIMESTAMP, timestamp)
        }

        // Inserisci la nuova riga, restituendo il valore della chiave primaria della nuova riga
        val newRowId = db?.insert(DbHelper.UserActivityEntry.TABLE_NAME, null, values)
        return newRowId
    }

    fun readUserActivity(dbHelper: DbHelper, user: String){
        val db = dbHelper.readableDatabase
        // Define a projection: the SELECT part of a query
        val projection = arrayOf(BaseColumns._ID,
            UserActivityEntry.COLUMN_NAME,
            UserActivityEntry.COLUMN_ACTIVITY_TYPE,
            UserActivityEntry.COLUMN_TRANSITION_TYPE,
            UserActivityEntry.COLUMN_TIMESTAMP,
            )
        val cursor = db.query( // Returns a Cursor
            UserActivityEntry.TABLE_NAME, // The table to query
            projection, // The array of columns to return (pass null to get all)
            "${UserActivityEntry.COLUMN_NAME} = ?",
            arrayOf(user),// The values for the WHERE clause (injected args)
            null, // GROUP BY
            null, // FILTER BY
            null // SORT
        )

        val items = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val item = getString(getColumnIndexOrThrow(UserActivityEntry.COLUMN_NAME))
                items.add(item)
            }
        }
        cursor.close()
    }
}