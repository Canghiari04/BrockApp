package com.example.brockapp.util

import com.example.brockapp.R
import com.example.brockapp.ACTIVITY_RECOGNITION_NOTIFY
import com.example.brockapp.NOTIFICATION_INTENT_TYPE

import android.content.Intent
import android.content.Context
import androidx.core.app.NotificationCompat

class NotificationUtil {
    fun getIntent(activityType: Int): Intent {
        return Intent().apply {
            setAction(NOTIFICATION_INTENT_TYPE)
            putExtra("title", "BrockApp")
            putExtra("content", "Registrazione attività avvenuta con successo.")
            putExtra("type", activityType.toString())
            putExtra("typeNotify", ACTIVITY_RECOGNITION_NOTIFY)
        }
    }

    fun getActivityRecognitionNotification(channelId: String, context: Context, intent: Intent): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(getIcon(intent.getStringExtra("type")))
            setContentTitle(intent.getStringExtra("title"))
            setContentText(intent.getStringExtra("content"))
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
    }

    private fun getIcon(type: String?): Int {
        return when (type) {
            "3" -> {
                R.drawable.baseline_chair_24
            }

            "0" -> {
                R.drawable.baseline_directions_car_24
            }

            "7" -> {
                R.drawable.baseline_directions_walk_24
            }

            else -> {
                R.drawable.baseline_directions_run_24
            }
        }
    }
}