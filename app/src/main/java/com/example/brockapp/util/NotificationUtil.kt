package com.example.brockapp.util

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.activity.PageLoaderActivity

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.provider.Settings
import androidx.core.text.HtmlCompat
import androidx.core.app.NotificationCompat

class NotificationUtil {
    fun getConnectivityPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONNECTIVITY_NOTIFY,
            Intent(Settings.ACTION_SETTINGS),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PageLoaderActivity::class.java).apply {
            putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONNECTIVITY_NOTIFY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getNotificationBody(
        channelId: String,
        title: String?,
        text: String?,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.baseline_directions_run_24)
            setContentTitle(HtmlCompat.fromHtml("<b>${title}</b>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            setContentText(text)
            setStyle(NotificationCompat.BigTextStyle()
                .bigText(text))
            setPriority(NotificationCompat.PRIORITY_HIGH)
        }
    }

    fun getNotificationBodyWithPendingIntent(
        channelId: String,
        title: String,
        content: String,
        pendingIntent: PendingIntent,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.baseline_directions_run_24)
            setContentTitle(HtmlCompat.fromHtml("<b>${title}</b>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            setContentText(content)
            setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            setPriority(NotificationCompat.PRIORITY_HIGH)
            addAction(R.drawable.baseline_more_horiz_24, "Open", pendingIntent)
        }
    }
}