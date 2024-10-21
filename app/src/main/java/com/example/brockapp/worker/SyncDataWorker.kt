package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.util.NotificationUtil
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.NotificationSender

import java.io.File
import android.util.Log
import com.google.gson.Gson
import androidx.work.Worker
import android.content.Context
import kotlinx.coroutines.launch
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import android.app.NotificationManager
import kotlinx.coroutines.CoroutineScope
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest

class SyncDataWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), NotificationSender {
    private lateinit var file: File
    private lateinit var db: BrockDB
    private lateinit var util: NotificationUtil
    private lateinit var s3Client: AmazonS3Client
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        util = NotificationUtil()
        db = BrockDB.getInstance(context)
        file = File(context.filesDir, "user_data.json")
        s3Client = MyS3ClientProvider.getInstance(context)

        // syncData()

        return Result.success()
    }

    override fun sendNotification(title: String, content: String) {
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getNotificationBody(
            CHANNEL_ID_MEMO_NOTIFY,
            title,
            content,
            context
        )

        manager.notify(ID_MEMO, notification.build())
    }

    private fun syncData() {
        CoroutineScope(Dispatchers.IO).launch {
            val geofence = db
                .GeofenceTransitionDao()
                .getAllGeofenceTransitionByUserId(MyUser.id)

            val walkActivities = db
                .UserWalkActivityDao()
                .getWalkActivitiesByUserId(MyUser.id)

            val vehicleActivities = db
                .UserVehicleActivityDao()
                .getVehicleActivitiesByUserId(MyUser.id)

            val stillActivities = db
                .UserStillActivityDao()
                .getStillActivitiesByUserId(MyUser.id)

            val userData = mapOf(
                "username" to MyUser.username,
                "typeActivity" to MyUser.typeActivity,
                "country" to MyUser.country,
                "city" to MyUser.city,
                "vehicleActivities" to vehicleActivities,
                "stillActivities" to stillActivities,
                "walkActivities" to walkActivities,
                "geofenceTransitions" to geofence
            )

            val gson = Gson()
            val json = gson.toJson(userData)

            file.writeText(json)

            try {
                val request =
                    PutObjectRequest(BUCKET_NAME, "user/${MyUser.username}.json", file)
                s3Client.putObject(request)
            } catch (e: Exception) {
                Log.e("SYNC_DATA_SERVICE", "Failed to upload user data $e")
            }

            sendNotification(
                "BrockApp - Sync data done!",
                "Your data has been correctly uploaded"
            )
        }
    }
}