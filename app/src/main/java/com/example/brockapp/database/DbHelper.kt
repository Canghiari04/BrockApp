package com.example.brockapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.brockapp.DATABASE_NAME
import com.example.brockapp.DATABASE_VERSION
import com.example.brockapp.DATE_FORMAT
import com.example.brockapp.User
import com.example.brockapp.mapper.UserStillActivityMapper
import com.example.brockapp.mapper.UserVehicleActivityMapper
import com.example.brockapp.mapper.UserWalkActivityMapper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar


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

    fun insertUserStillActivity (userId: Long, transitionType: Int, timestamp: String) : Long? {
        val activityId = getNextActivityId()
        updateNextActivityId()

        val contentValues = ContentValues().apply {
            put(UserVehicleActivity.ID, activityId)
            put(UserVehicleActivity.USER_ID, userId)
            put(UserVehicleActivity.TRANSITION_TYPE, transitionType)
            put(UserVehicleActivity.TIMESTAMP, timestamp)
        }

        return this.writableDatabase?.insert(UserStillActivity.TABLE_NAME, null, contentValues)

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

    fun getUserWalkActivities(userId: Long, startOfDay: String, endOfDay : String) : ArrayList<UserWalkActivityMapper>{
        val db = this.readableDatabase
        var list = ArrayList<UserWalkActivityMapper>()


        val args = arrayOf(userId.toString(), 1.toString(), startOfDay, endOfDay)
        val cursor = db.rawQuery("SELECT * FROM ${UserWalkActivity.TABLE_NAME} WHERE ${UserWalkActivity.USER_ID} = ? AND ${UserWalkActivity.TRANSITION_TYPE} = ? AND TIMESTAMP BETWEEN ? AND ?", args)

        while(cursor.moveToNext()) {
            val activityId = cursor.getLong(0)
            val userId = cursor.getLong(1)
            val transitionType = cursor.getInt(2)
            val timestamp = cursor.getString(3)
            val stepCount = cursor.getLong(4)

            val activity = UserWalkActivityMapper(
                activityId,
                userId,
                transitionType,
                timestamp,
                stepCount
            )
            list.add(activity)
        }

        cursor.close()

        return list
    }

    fun getUserVehicleActivities(userId: Long, startOfDay: String, endOfDay : String): List<UserVehicleActivityMapper> {
        val db = this.readableDatabase
        var list = ArrayList<UserVehicleActivityMapper>()


        val args = arrayOf(userId.toString(), 1.toString(), startOfDay, endOfDay)
        val cursor = db.rawQuery("SELECT * FROM ${UserVehicleActivity.TABLE_NAME} WHERE ${UserVehicleActivity.USER_ID} = ? AND ${UserVehicleActivity.TRANSITION_TYPE} = ? AND TIMESTAMP BETWEEN ? AND ?", args)

        while(cursor.moveToNext()) {
            val activityId = cursor.getLong(0)
            val userId = cursor.getLong(1)
            val transitionType = cursor.getInt(2)
            val timestamp = cursor.getString(3)
            val distanceTravelled = cursor.getDouble(4)

            val activity = UserVehicleActivityMapper(
                activityId,
                userId,
                transitionType,
                timestamp,
                distanceTravelled
            )
            list.add(activity)
        }

        cursor.close()
        return list
    }

    fun getUserStillActivities(userId: Long, startOfDay: String, endOfDay : String): List<UserStillActivityMapper> {
        val db = this.readableDatabase
        var list = ArrayList<UserStillActivityMapper>()


        val args = arrayOf(userId.toString(), 1.toString(), startOfDay, endOfDay)
        val cursor = db.rawQuery("SELECT * FROM ${UserStillActivity.TABLE_NAME} WHERE ${UserStillActivity.USER_ID} = ? AND ${UserStillActivity.TRANSITION_TYPE} = ? AND TIMESTAMP BETWEEN ? AND ?", args)

        while(cursor.moveToNext()) {
            val activityId = cursor.getLong(0)
            val userId = cursor.getLong(1)
            val transitionType = cursor.getInt(2)
            val timestamp = cursor.getString(3)

            val activity = UserStillActivityMapper(
                activityId,
                userId,
                transitionType,
                timestamp
            )

            list.add(activity)
        }

        cursor.close()
        return list
    }


    fun getDayRange(dateStr: String): Pair<String, String> {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy")
        val outputFormat = SimpleDateFormat(DATE_FORMAT)
        val date = inputFormat.parse(dateStr)

        // Inizio del giorno
        val calendarStart = Calendar.getInstance()
        calendarStart.time = date
        calendarStart.set(Calendar.HOUR_OF_DAY, 0)
        calendarStart.set(Calendar.MINUTE, 0)
        calendarStart.set(Calendar.SECOND, 0)
        val startOfDay = outputFormat.format(calendarStart.time)

        // Fine del giorno
        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = date
        calendarEnd.set(Calendar.HOUR_OF_DAY, 23)
        calendarEnd.set(Calendar.MINUTE, 59)
        calendarEnd.set(Calendar.SECOND, 59)
        val endOfDay = outputFormat.format(calendarEnd.time)

        return Pair(startOfDay, endOfDay)
    }
}