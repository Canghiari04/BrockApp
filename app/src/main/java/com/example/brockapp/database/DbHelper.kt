package com.example.brockapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.brockapp.DATABASE_NAME
import com.example.brockapp.DATABASE_VERSION
import com.example.brockapp.User
import com.example.brockapp.mapper.UserStillActivityMapper
import com.example.brockapp.mapper.UserVehicleActivityMapper
import com.example.brockapp.mapper.UserWalkActivityMapper
import java.time.LocalDate


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

    object ActivityIdSequence{
        const val TABLE_NAME = "activity_id_sequence"
        const val ID = "next_id"
    }

    object UserActivityEntry {
        const val TABLE_NAME = "user_activity"
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ACTIVITY_TYPE = "activity_type"
        const val TRANSITION_TYPE = "transition_type"
        const val TIMESTAMP = "timestamp"
    }

    object UserWalkActivity {
        const val TABLE_NAME = "user_walk_activity"
        const val ID = "id"
        const val USER_ID = "user_id"
        const val TRANSITION_TYPE = "transition_type"
        const val TIMESTAMP = "timestamp"
        const val STEP_NUMBER = "step_number"
    }

    object UserVehicleActivity {
        const val TABLE_NAME = "user_vehicle_activity"
        const val ID = "id"
        const val USER_ID = "user_id"
        const val TRANSITION_TYPE = "transition_type"
        const val TIMESTAMP = "timestamp"
        const val DISTANCE_TRAVELLED = "distance_travelled"
    }

    object UserStillActivity {
        const val TABLE_NAME = "user_still_activity"
        const val ID = "id"
        const val USER_ID = "user_id"
        const val TRANSITION_TYPE = "transition_type"
        const val TIMESTAMP = "timestamp"
    }

    /**
     * Crea le tabelle interne al database.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS ${UserEntry.TABLE_NAME} (${UserEntry.ID} INTEGER PRIMARY KEY, " +
                " ${UserEntry.USERNAME} TEXT, ${UserEntry.PASSWORD} TEXT)")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${UserIdSequence.TABLE_NAME} (${UserIdSequence.ID} INTEGER PRIMARY KEY)")
        db.execSQL("INSERT INTO ${UserIdSequence.TABLE_NAME} (${UserIdSequence.ID}) VALUES (1)")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${ActivityIdSequence.TABLE_NAME} (${ActivityIdSequence.ID} INTEGER PRIMARY KEY)")
        db.execSQL("INSERT INTO ${ActivityIdSequence.TABLE_NAME} (${ActivityIdSequence.ID}) VALUES (1)")

        db.execSQL("DROP TABLE IF EXISTS ${UserActivityEntry.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${UserWalkActivity.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${UserVehicleActivity.TABLE_NAME}")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${UserWalkActivity.TABLE_NAME} (${UserWalkActivity.ID} INTEGER PRIMARY KEY, " +
                "${UserWalkActivity.USER_ID} LONG, ${UserWalkActivity.TRANSITION_TYPE} INTEGER, ${UserWalkActivity.TIMESTAMP} TEXT, ${UserWalkActivity.STEP_NUMBER} INTEGER)")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${UserVehicleActivity.TABLE_NAME} (${UserVehicleActivity.ID} INTEGER PRIMARY KEY, " +
                "${UserWalkActivity.USER_ID} LONG, ${UserVehicleActivity.TRANSITION_TYPE} INTEGER, ${UserVehicleActivity.TIMESTAMP} TEXT, ${UserVehicleActivity.DISTANCE_TRAVELLED} DOUBLE)")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${UserStillActivity.TABLE_NAME} (${UserStillActivity.ID} INTEGER PRIMARY KEY, " +
                "${UserStillActivity.USER_ID} LONG, ${UserStillActivity.TRANSITION_TYPE} INTEGER, ${UserStillActivity.TIMESTAMP} TEXT)")
    }

    // Aggiornare la versione del database ogni volta che si voglia modificare la struttura delle tabelle.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS ${UserActivityEntry.TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${UserWalkActivity.TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${UserVehicleActivity.TABLE_NAME}")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${UserWalkActivity.TABLE_NAME} (${UserWalkActivity.ID} INTEGER PRIMARY KEY, " +
                    "${UserWalkActivity.USER_ID} LONG, ${UserWalkActivity.TRANSITION_TYPE} INTEGER, ${UserWalkActivity.TIMESTAMP} TEXT, ${UserWalkActivity.STEP_NUMBER} INTEGER)")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${UserVehicleActivity.TABLE_NAME} (${UserVehicleActivity.ID} INTEGER PRIMARY KEY, " +
                    "${UserWalkActivity.USER_ID} LONG, ${UserVehicleActivity.TRANSITION_TYPE} INTEGER, ${UserVehicleActivity.TIMESTAMP} TEXT, ${UserVehicleActivity.DISTANCE_TRAVELLED} DOUBLE)")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${UserStillActivity.TABLE_NAME} (${UserStillActivity.ID} INTEGER PRIMARY KEY, " +
                    "${UserStillActivity.USER_ID} LONG, ${UserStillActivity.TRANSITION_TYPE} INTEGER, ${UserStillActivity.TIMESTAMP} TEXT)")
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

    @Synchronized
    fun getNextActivityId() : Long {
        var nextId = -1L
        val cursor = this.readableDatabase.rawQuery("SELECT next_id FROM activity_id_sequence", null)
        if (cursor.moveToFirst()) {
            nextId = cursor.getLong(0)
        }
        cursor.close()
        return nextId
    }
    @Synchronized
    fun updateNextActivityId() {
        this.writableDatabase.execSQL("UPDATE ${ActivityIdSequence.TABLE_NAME} SET ${ActivityIdSequence.ID} = ${ActivityIdSequence.ID} + 1")
    }

    fun insertUserWalkActivity(userId: Long, transitionType: Int, timestamp: String, stepNumber: Long) : Long? {
        val activityId = getNextActivityId()
        updateNextActivityId()

        val values = ContentValues().apply {
            put(UserWalkActivity.ID, activityId)
            put(UserWalkActivity.USER_ID, userId)
            put(UserWalkActivity.TRANSITION_TYPE, transitionType)
            put(UserWalkActivity.TIMESTAMP, timestamp)
            put(UserWalkActivity.STEP_NUMBER, stepNumber)

        }
        val newRowId = this.writableDatabase?.insert(UserWalkActivity.TABLE_NAME, null, values)

        return newRowId

    }

    fun insertUserVehicleActivity(userId: Long, transitionType: Int, timestamp: String, distanceTravelled: Double): Long? {
        val activityId = getNextActivityId()
        updateNextActivityId()

        val contentValues = ContentValues().apply {
            put(UserVehicleActivity.ID, activityId)
            put(UserVehicleActivity.USER_ID, userId)
            put(UserVehicleActivity.TRANSITION_TYPE, transitionType)
            put(UserVehicleActivity.TIMESTAMP, timestamp)
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

    fun getUserWalkActivities(userId: Long, date: String) : ArrayList<String>{
        val db = this.readableDatabase
        var list = ArrayList<String>()

        val args = arrayOf(userId.toString(), 1.toString())
        val cursor = db.rawQuery("SELECT ${UserWalkActivity.USER_ID}, ${UserWalkActivity.STEP_NUMBER}, ${UserWalkActivity.TIMESTAMP} FROM ${UserWalkActivity.TABLE_NAME} WHERE ${UserWalkActivity.USER_ID} = ? AND ${UserWalkActivity.TRANSITION_TYPE} = ?", args)

        while(cursor.moveToNext()) {
            val timestamp = cursor.getString(2)
            list.add(timestamp)
        }

        cursor.close()


//        val activities = mutableListOf<UserWalkActivityMapper>()
//        val columns = arrayOf("ID", "USER_ID", "TRANSITION_TYPE", "TIMESTAMP", "STEP_NUMBER")
//        val selection = "USER_ID = ? AND TIMESTAMP = ? AND TRANSITION_TYPE = 1"
//        val selectionArgs = arrayOf(userId.toString(), date)
//
//        val cursor: Cursor = db.query(UserWalkActivity.TABLE_NAME, columns, selection, selectionArgs, null, null, null)
//        while (cursor.moveToNext()) {
//            val activity = UserWalkActivityMapper(
//                id = cursor.getLong(cursor.getColumnIndexOrThrow("ID")),
//                userId = cursor.getLong(cursor.getColumnIndexOrThrow("USER_ID")),
//                transitionType = cursor.getInt(cursor.getColumnIndexOrThrow("TRANSITION_TYPE")),
//                timestamp = cursor.getString(cursor.getColumnIndexOrThrow("TIMESTAMP")),
//                stepNumber = cursor.getInt(cursor.getColumnIndexOrThrow("STEP_NUMBER"))
//            )
//            activities.add(activity)
//        }
//
//        cursor.close()
//        return activities
        return list
    }

    fun getUserVehicleActivities(userId: Long, date: String): List<UserVehicleActivityMapper> {
        val db = this.readableDatabase

        val activities = mutableListOf<UserVehicleActivityMapper>()
        val columns = arrayOf("ID", "USER_ID", "TRANSITION_TYPE", "TIMESTAMP", "DISTANCE_TRAVELLED")
        val selection = "USER_ID = ? AND DATE(TIMESTAMP) = ? AND TRANSITION_TYPE = 1"
        val selectionArgs = arrayOf(userId.toString(), date)

        val cursor: Cursor = db.query(UserVehicleActivity.TABLE_NAME, columns, selection, selectionArgs, null, null, "TIMESTAMP")
        while (cursor.moveToNext()) {
            val activity = UserVehicleActivityMapper(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("ID")),
                userId = cursor.getLong(cursor.getColumnIndexOrThrow("USER_ID")),
                transitionType = cursor.getInt(cursor.getColumnIndexOrThrow("TRANSITION_TYPE")),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow("TIMESTAMP")),
                distanceTravelled = cursor.getDouble(cursor.getColumnIndexOrThrow("DISTANCE_TRAVELLED"))
            )
            activities.add(activity)
        }

        cursor.close()
        return activities
    }

    fun getUserStillActivities(userId: Long, date: String): List<UserStillActivityMapper> {
        val db = this.readableDatabase

        val activities = mutableListOf<UserStillActivityMapper>()
        val columns = arrayOf("ID", "USER_ID", "TRANSITION_TYPE", "TIMESTAMP")
        val selection = "USER_ID = ? AND DATE(TIMESTAMP) = ? AND TRANSITION_TYPE = 1"
        val selectionArgs = arrayOf(userId.toString(), date)

        val cursor: Cursor = db.query(UserStillActivity.TABLE_NAME, columns, selection, selectionArgs, null, null, "TIMESTAMP")
        while (cursor.moveToNext()) {
            val activity = UserStillActivityMapper(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("ID")),
                userId = cursor.getLong(cursor.getColumnIndexOrThrow("USER_ID")),
                transitionType = cursor.getInt(cursor.getColumnIndexOrThrow("TRANSITION_TYPE")),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow("TIMESTAMP"))
            )
            activities.add(activity)
        }

        cursor.close()
        return activities
    }
}