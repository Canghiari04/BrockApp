package com.example.brockapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.brockapp.CHANNEL_ID_ACTIVITY_NOTIFY
import com.example.brockapp.DESCRIPTION_CHANNEL_ACTIVITY_NOTIFY
import com.example.brockapp.ID_ACTIVITY_NOTIFY
import com.example.brockapp.NAME_CHANNEL_ACTIVITY_NOTIFY
import com.example.brockapp.util.NotificationUtil

class ActivityRecognitionWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
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