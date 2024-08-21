package com.example.brockapp.worker

import com.example.brockapp.*
import com.example.brockapp.util.NotificationUtil

import androidx.work.Worker
import android.content.Context
import androidx.work.WorkerParameters
import android.app.NotificationManager
import android.app.NotificationChannel

class ActivityRecognitionWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun doWork(): Result {
        val type = inputData.getString("type")?.toInt()
        val title = inputData.getString("title")
        val text = inputData.getString("text")

        util = NotificationUtil()
        sendActivityNotify(type, title, text)

        return Result.success()
    }

    private fun sendActivityNotify(type: Int?, title: String?, text: String?) {
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = util.getActivityRecognitionNotification(CHANNEL_ID_ACTIVITY_NOTIFY, type, title, text, context)

        getNotificationChannel()

        manager.notify(ID_ACTIVITY_NOTIFY, notification.build())
    }

    private fun getNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_ACTIVITY_NOTIFY,
            NAME_CHANNEL_ACTIVITY_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_ACTIVITY_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }
}