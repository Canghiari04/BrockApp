package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.singleton.MyS3ClientProvider

import java.io.File
import android.util.Log
import com.google.gson.Gson
import androidx.work.Worker
import android.content.Context
import kotlinx.coroutines.launch
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest

class SyncBucketWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private lateinit var file: File
    private lateinit var db: BrockDB
    private lateinit var util: NotificationUtil
    private lateinit var s3Client: AmazonS3Client

    override fun doWork(): Result {
        util = NotificationUtil()
        db = BrockDB.getInstance(context)
        s3Client = MyS3ClientProvider.getInstance(context)
        file = File(context.filesDir, "user_data.json")

        syncBucket()

        return Result.success()
    }

    private fun syncBucket() {
        CoroutineScope(Dispatchers.IO).launch {
            val vehicleActivities = db
                .UsersVehicleActivityDao()
                .getVehicleActivitiesByUsername(MyUser.username)

            val runActivities = db
                .UsersRunActivityDao()
                .getRunActivitiesByUsername(MyUser.username)

            val stillActivities = db
                .UsersStillActivityDao()
                .getStillActivitiesByUsername(MyUser.username)

            val walkActivities = db
                .UsersWalkActivityDao()
                .getWalkActivitiesByUsername(MyUser.username)

            val geofence = db
                .GeofenceTransitionsDao()
                .getGeofenceTransitionsByUsername(MyUser.username)

            val userData = mapOf(
                "username" to MyUser.username,
                "typeActivity" to MyUser.typeActivity,
                "country" to MyUser.country,
                "city" to MyUser.city,
                "vehicleActivities" to vehicleActivities,
                "runActivities" to runActivities,
                "stillActivities" to stillActivities,
                "walkActivities" to walkActivities,
                "geofenceTransitions" to geofence
            )

            val gson = Gson()
            val json = gson.toJson(userData)

            file.writeText(json)

            try {
                val request =
                    PutObjectRequest(BuildConfig.BUCKET_NAME, "user/${MyUser.username}.json", file)
                s3Client.putObject(request)
            } catch (e: Exception) {
                Log.e("SYNC_DATA_SERVICE", "Failed to upload user data $e")
            }
        }
    }
}