package com.example.brockapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.brockapp.DATABASE_NAME


const val DATABASE_VERSION = 2
class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


        // Table contents are grouped together in an anonymous object.
    object UserActivityEntry : BaseColumns {
        const val TABLE_NAME = "user_activity"
        const val ID = "id"
        const val NAME = "name"
        const val USER_ID = "user_id"
        const val ACTIVITY_TYPE = "activity_type"
        const val TRANSITION_TYPE = "transition_type"
        const val TIMESTAMP = "timestamp"
    }


    object UserEntry {
        const val TABLE_NAME = "user"
        const val ID = "id"
        const val USERNAME = "username"
        const val PASSWORD = "password"

    }
    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            "CREATE TABLE ${UserEntry.TABLE_NAME} (${UserEntry.ID} LONG PRIMARY KEY, " +
                    "${UserEntry.USERNAME} TEXT , ${UserEntry.PASSWORD} TEXT)"
        )

        db.execSQL(
            "CREATE TABLE ${UserActivityEntry.TABLE_NAME} (${UserActivityEntry.ID} INTEGER PRIMARY KEY, " +
                    "${UserActivityEntry.NAME} TEXT, ${UserActivityEntry.USER_ID} LONG REFERENCES ${UserEntry.TABLE_NAME}(${UserEntry.ID}), ${UserActivityEntry.ACTIVITY_TYPE} TEXT," +
                    "${UserActivityEntry.TRANSITION_TYPE} TEXT, ${UserActivityEntry.TIMESTAMP} LONG)"
        )

    }

    /*
     * Upgradare la versione del db ogni volta che si vuole modificare la struttura delle tabelle
     *
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE ${UserEntry.TABLE_NAME} RENAME TO ${UserEntry.TABLE_NAME}_old")

            db.execSQL(
                "CREATE TABLE ${UserEntry.TABLE_NAME} (${UserEntry.ID} LONG PRIMARY KEY, " +
                        "${UserEntry.USERNAME} TEXT , ${UserEntry.PASSWORD} TEXT)"
            )
            db.execSQL(
                "INSERT INTO ${UserEntry.TABLE_NAME} (${UserEntry.ID}, ${UserEntry.USERNAME}, ${UserEntry.PASSWORD}) " +
                        "SELECT ${UserEntry.ID}, ${UserEntry.USERNAME}, ${UserEntry.PASSWORD} FROM ${UserEntry.TABLE_NAME}_old"
            )

            db.execSQL("DROP TABLE ${UserEntry.TABLE_NAME}_old")
            db.execSQL("ALTER TABLE ${UserActivityEntry.TABLE_NAME} ADD COLUMN ${UserActivityEntry.USER_ID} LONG REFERENCES ${UserEntry.TABLE_NAME}(${UserEntry.ID})")
        }
    }
    fun insertUser(dbHelper: DbHelper, username: String, password: String) : Long? {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(UserEntry.USERNAME, username)
            put(UserEntry.PASSWORD, password)
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
            put(UserActivityEntry.NAME, name)
            put(UserActivityEntry.ACTIVITY_TYPE, activityType)
            put(UserActivityEntry.TRANSITION_TYPE, transitionType)
            put(UserActivityEntry.TIMESTAMP, timestamp)
        }

        // Inserisci la nuova riga, restituendo il valore della chiave primaria della nuova riga
        val newRowId = db?.insert(UserActivityEntry.TABLE_NAME, null, values)
        return newRowId
    }

    fun readUserActivity(dbHelper: DbHelper, user: String){
        val db = dbHelper.readableDatabase
        // Define a projection: the SELECT part of a query
        val projection = arrayOf(BaseColumns._ID,
            UserActivityEntry.NAME,
            UserActivityEntry.ACTIVITY_TYPE,
            UserActivityEntry.TRANSITION_TYPE,
            UserActivityEntry.TIMESTAMP,
            )
        val cursor = db.query(
            UserActivityEntry.TABLE_NAME,
            projection,
            "${UserActivityEntry.NAME} = ?",
            arrayOf(user),
            null,
            null,
            null
        )

        val items = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val item = getString(getColumnIndexOrThrow(UserActivityEntry.NAME))
                items.add(item)
            }
        }
        cursor.close()
    }

    fun checkIfUserExists(username: String, password: String): Boolean {
        val db = this.readableDatabase

        val selection = "${UserEntry.USERNAME} = ? AND ${UserEntry.PASSWORD} = ?"
        val selectionArgs = arrayOf(username, password)

        val cursor = db.query(
            UserEntry.TABLE_NAME,
            arrayOf(UserEntry.ID),
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