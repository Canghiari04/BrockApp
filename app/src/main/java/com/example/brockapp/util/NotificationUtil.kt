package com.example.brockapp.util

import com.example.brockapp.*
import com.example.brockapp.R

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.provider.Settings
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

    fun getActivityRecognitionNotification(
        channelId: String,
        activityType: Int?,
        title: String?,
        text: String?,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(getActivityRecognitionIcon(activityType))
            setContentTitle(title)
            setContentText(text)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
    }

    fun getGeofenceNotification(
        channelId: String,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.baseline_directions_run_24)
            setContentTitle("Sei entrato in una zona d'interesse!")
            setContentText("Hai appena varcato i confini di una zona di interesse")
            setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hai appena varcato i confini di una zona di interesse. " +
                        "Inizia a registrare le tue attività di oggi!!"))
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }
    }

    fun getConnectivityNotification(
        channelId: String,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.baseline_directions_run_24)
            setContentTitle("Connessione rilevata")
            setContentText("Le funzionalità disabilitate sono state attivate")
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }
    }

    fun getErrorConnectivityNotification(
        channelId: String,
        pendingIntent: PendingIntent,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.baseline_directions_run_24)
            setContentTitle("Connessione non rilevata")
            setContentText("Alcune funzionalità sono state disattivate")
            setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Alcune funzionalità sono state disattivate. " +
                        "Verifica le impostazioni di connessione sul tuo dispositivo " +
                        "per garantire che tutte le funzionalità operino correttamente"
                )
            )
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
            addAction(R.drawable.baseline_settings_applications_24, "Apri Impostazioni", pendingIntent)
        }
    }

    private fun getActivityRecognitionIcon(type: Int?): Int {
        return when (type) {
            3 -> {
                R.drawable.baseline_chair_24
            }

            0 -> {
                R.drawable.baseline_directions_car_24
            }

            7 -> {
                R.drawable.baseline_directions_walk_24
            }

            else -> {
                R.drawable.baseline_directions_run_24
            }
        }
    }
}