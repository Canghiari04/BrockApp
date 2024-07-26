package com.example.brockapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.brockapp.DATABASE_NAME
import com.example.brockapp.DATABASE_VERSION

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


        // Table contents are grouped together in an anonymous object.
    object UserActivityEntry : BaseColumns {
        const val TABLE_NAME = "user_activity"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ACTIVITY_TYPE = "activity_type"
        const val COLUMN_TRANSITION_TYPE = "transition_type"
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
            "CREATE TABLE ${UserEntry.TABLE_NAME} (${UserEntry.COLUMN_ID} INTEGER PRIMARY KEY, " +
                    "${UserEntry.COLUMN_USERNAME} TEXT , ${UserEntry.COLUMN_PASSWORD} TEXT)"
        )

        db.execSQL(
            "CREATE TABLE ${UserActivityEntry.TABLE_NAME} (${UserActivityEntry.COLUMN_ID} INTEGER PRIMARY KEY, " +
                    "${UserActivityEntry.COLUMN_NAME} TEXT, ${UserActivityEntry.COLUMN_ACTIVITY_TYPE} TEXT," +
                    "${UserActivityEntry.COLUMN_TRANSITION_TYPE} TEXT, ${UserActivityEntry.COLUMN_TIMESTAMP} LONG)"
        )

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun insertUser(dbHelper: DbHelper, username: String, password: String) : Long? {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(UserEntry.COLUMN_USERNAME, username)
            put(UserEntry.COLUMN_PASSWORD, password)
        }

        val newRowId = db?.insert(UserEntry.TABLE_NAME, null, contentValues)
        if (newRowId == -1L) {
            Log.e("DB_INSERT", "Errore durante l'inserimento dell'utente: $username")
        } else {
            Log.d("DB_INSERT", "Inserimento riuscito con ID: $newRowId")
        }
        return newRowId
    }


    fun insertUserActivity(dbHelper: DbHelper, name: String, activityType: String, transitionType: String, timestamp: Long
    ) : Long?{
        // Ottieni il database in modalità scrittura
        val db = dbHelper.writableDatabase

        // Crea una nuova mappa di valori, dove i nomi delle colonne sono le chiavi
        val values = ContentValues().apply {
            put(UserActivityEntry.COLUMN_NAME, name)
            put(UserActivityEntry.COLUMN_ACTIVITY_TYPE, activityType)
            put(UserActivityEntry.COLUMN_TRANSITION_TYPE, transitionType)
            put(UserActivityEntry.COLUMN_TIMESTAMP, timestamp)
        }

        // Inserisci la nuova riga, restituendo il valore della chiave primaria della nuova riga
        val newRowId = db?.insert(UserActivityEntry.TABLE_NAME, null, values)
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
        val cursor = db.query(
            UserActivityEntry.TABLE_NAME,
            projection,
            "${UserActivityEntry.COLUMN_NAME} = ?",
            arrayOf(user),
            null,
            null,
            null
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

    fun checkIfUserExists(username: String, password: String): Boolean {
        val db = this.readableDatabase

        val selection = "${UserEntry.COLUMN_USERNAME} = ? AND ${UserEntry.COLUMN_PASSWORD} = ?"
        val selectionArgs = arrayOf(username, password)

        val cursor = db.query(
            UserEntry.TABLE_NAME,
            arrayOf(UserEntry.COLUMN_ID),
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val userExists = cursor.count > 0
        cursor.close()
        return userExists


    }
}