package com.example.brockapp.util

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.activity.MainActivity

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.provider.Settings
import androidx.core.text.HtmlCompat
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.DetectedActivity

class NotificationUtil {

    private val mapperActivityNotification = mapOf(
        DetectedActivity.IN_VEHICLE to "You're driving. Stay focus, don't use your telephone",
        DetectedActivity.RUNNING to "You're running! You're doing great",
        DetectedActivity.WALKING to "You're walking! Keep up the good work",
        DetectedActivity.STILL to "You are currently idle. Try moving to stay active"
    )

    fun getConnectivityPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONNECTIVITY_NOTIFY,
            Intent(Settings.ACTION_SETTINGS),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_map)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONNECTIVITY_NOTIFY,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getNotificationBody(
        channelId: String,
        icon: Int,
        title: String?,
        text: String?,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(icon)
            setContentTitle(HtmlCompat.fromHtml("<b>${title}</b>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            setContentText(text)
            setStyle(NotificationCompat.BigTextStyle()
                .bigText(text))
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
    }

    fun getNotificationBodyWithPendingIntent(
        channelId: String,
        icon: Int,
        title: String,
        content: String,
        pendingIntent: PendingIntent,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(icon)
            setContentTitle(HtmlCompat.fromHtml("<b>${title}</b>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            setContentText(content)
            setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            setPriority(NotificationCompat.PRIORITY_HIGH)
            addAction(R.drawable.icon_more, "Open", pendingIntent)
        }
    }

    @SuppressLint("MissingPermission")
    fun updateActivityRecognitionNotification(
        notificationId: Int,
        type: Int,
        context: Context
    ) {
        val manager = NotificationManagerCompat.from(context)

        manager.notify(
            notificationId,
            getNotificationBody(
                CHANNEL_ID_ACTIVITY_RECOGNITION_WORKER,
                R.drawable.icon_run,
                "BrockApp - Activity Recognition Service",
                "${mapperActivityNotification[type]}",
                context
            ).build()
        )
    }
}