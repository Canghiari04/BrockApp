package com.example.brockapp.database

import com.example.brockapp.DATABASE_NAME
import com.example.brockapp.DATABASE_VERSION

import android.util.Log
import android.content.Context
import android.provider.BaseColumns
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    object UserEntry {
        const val TABLE_NAME = "user"
        const val ID = "id"
        const val USERNAME = "username"
        const val PASSWORD = "password"
    }

    object UserIdSequence {
        const val TABLE_NAME = "user_id_sequence"
        const val ID = "next_id"
    }

    object UserActivityEntry {
        const val TABLE_NAME = "user_activity"
        const val ID = "id"
        const val NAME = "name"
        const val USER_ID = "user_id"
        const val ACTIVITY_TYPE = "activity_type"
        const val TRANSITION_TYPE = "transition_type"
        const val TIMESTAMP = "timestamp"
    }

    object UserWalkActivity {
        const val TABLE_NAME = "user_walk_activity"
        const val ID = "id"
        const val USER_ID = "user_id"
        const val STEP_NUMBER = "step_number"
    }

    object UserVehicleActivity {
        const val TABLE_NAME = "user_vehicle_activity"
        const val ID = "id"
        const val USER_ID = "user_id"
        const val DISTANCE_TRAVELLED = "distance_travelled"
    }

    /**
     * Crea le tabelle interne al database.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${UserEntry.TABLE_NAME} (${UserEntry.ID} LONG PRIMARY KEY, " +
                 "${UserEntry.USERNAME} TEXT , ${UserEntry.PASSWORD} TEXT)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS ${UserIdSequence.TABLE_NAME} (${UserIdSequence.ID} INTEGER PRIMARY KEY)"
        )

        db.execSQL(
            "CREATE TABLE ${UserActivityEntry.TABLE_NAME} (${UserActivityEntry.ID} INTEGER PRIMARY KEY, " +
                 "${UserActivityEntry.NAME} TEXT, ${UserActivityEntry.USER_ID} LONG REFERENCES ${UserEntry.TABLE_NAME}(${UserEntry.ID}), ${UserActivityEntry.ACTIVITY_TYPE} TEXT," +
                 "${UserActivityEntry.TRANSITION_TYPE} TEXT, ${UserActivityEntry.TIMESTAMP} LONG)"
        )
    }

    // Aggiornare la versione del database ogni volta che si voglia modificare la struttura delle tabelle.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS ${UserWalkActivity.TABLE_NAME} (${UserWalkActivity.ID} INTEGER PRIMARY KEY, " +
                    "${UserWalkActivity.USER_ID} LONG REFERENCES ${UserEntry.TABLE_NAME}(${UserEntry.ID}), ${UserWalkActivity.STEP_NUMBER} INTEGER)")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${UserVehicleActivity.TABLE_NAME} (${UserVehicleActivity.ID} INTEGER PRIMARY KEY, " +
                    "${UserVehicleActivity.USER_ID} LONG REFERENCES ${UserEntry.TABLE_NAME}(${UserEntry.ID}), ${UserVehicleActivity.DISTANCE_TRAVELLED} DOUBLE)")
        }
    }

    fun insertUser(dbHelper: DbHelper, username: String, password: String) : Long {
        val userId = getNextUserId()

        updateNextUserId()

        val contentValues = ContentValues().apply {
            put(UserEntry.ID, userId)
            put(UserEntry.USERNAME, username)
            put(UserEntry.PASSWORD, password)
        }

        val newRowId = dbHelper.writableDatabase.insert(UserEntry.TABLE_NAME, null, contentValues)
        if (newRowId == -1L) {
            Log.e("DB_INSERT", "Errore durante l'inserimento dell'utente: $username")
        } else {
            Log.d("DB_INSERT", "Inserimento riuscito con ID: $newRowId")
        }

        return newRowId
    }

    @Synchronized
    fun getNextUserId() : Long {
        var nextId = -1L

        val cursor = this.readableDatabase.rawQuery("SELECT next_id FROM user_id_sequence", null)
        if (cursor.moveToFirst()) {
            nextId = cursor.getLong(0)
        }

        cursor.close()
        return nextId
    }

    @Synchronized
    fun updateNextUserId() {
        this.writableDatabase.execSQL("UPDATE ${UserIdSequence.TABLE_NAME} SET ${UserIdSequence.ID} = ${UserIdSequence.ID} + 1")
    }

    fun insertUserActivity(activityType: String, transitionType: String, timestamp: String, userId: Long) : Long?{
        val values = ContentValues().apply {
            put(UserActivityEntry.ACTIVITY_TYPE, activityType)
            put(UserActivityEntry.TRANSITION_TYPE, transitionType)
            put(UserActivityEntry.TIMESTAMP, timestamp)
            put(UserActivityEntry.USER_ID, userId)
        }

        val newRowId = this.writableDatabase?.insert(UserActivityEntry.TABLE_NAME, null, values)

        return newRowId
    }

    fun readUserActivity(dbHelper: DbHelper, user: String) {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(BaseColumns._ID,
            UserActivityEntry.ACTIVITY_TYPE,
            UserActivityEntry.TRANSITION_TYPE,
            UserActivityEntry.TIMESTAMP,
            UserActivityEntry.USER_ID
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

    fun insertUserWalkActivity(userId: Long, stepNumber: Long) : Long? {
        val values = ContentValues().apply {
            put(UserWalkActivity.USER_ID, userId)
            put(UserWalkActivity.STEP_NUMBER, stepNumber)

        }

        val newRowId = this.writableDatabase?.insert(UserActivityEntry.TABLE_NAME, null, values)

        return newRowId

    }

    fun insertUserVehicleActivity(userId: Long, distanceTravelled: Double): Long? {
        val contentValues = ContentValues().apply {
            put(UserVehicleActivity.USER_ID, userId)
            put(UserVehicleActivity.DISTANCE_TRAVELLED, distanceTravelled)
        }

        return this.writableDatabase?.insert(UserVehicleActivity.TABLE_NAME, null, contentValues)
    }



    fun getUserId(username: String, password: String) : Long {
        val db = this.readableDatabase

        val selection = "${UserEntry.USERNAME} = ? AND ${UserEntry.PASSWORD} = ?"
        val selectionArgs = arrayOf(username, password)

        var userId = -1L

        val cursor = db.query(
            UserEntry.TABLE_NAME,
            arrayOf(UserEntry.ID),
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            // Ottiene l'ID dell'utente dalla colonna 0 del risultato della query
            userId = cursor.getLong(0)
        }

        cursor.close()
        return userId
    }

    fun checkIfUserIsPresent(username: String, password: String): Boolean {
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

        val isPresent = cursor.count > 0
        cursor.close()
        return isPresent
    }
}