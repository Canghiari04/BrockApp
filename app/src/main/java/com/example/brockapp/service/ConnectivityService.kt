package com.example.brockapp.service

import com.example.brockapp.*
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.util.NotificationUtil

import android.util.Log
import android.Manifest
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.Context
import android.app.NotificationManager
import android.app.NotificationChannel
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices

class ConnectivityService: Service() {
    private lateinit var geofence: MyGeofence
    private lateinit var util: NotificationUtil
    private lateinit var manager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        geofence = MyGeofence.getInstance()
        util = NotificationUtil()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val (significantChange, typeNetwork) = handleSignificantConnectivityChange(this)

        if (significantChange) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val geofenceClient = LocationServices.getGeofencingClient(this)

                geofenceClient.removeGeofences(geofence.pendingIntent).run {
                    addOnSuccessListener {
                        Log.d("CONNECTIVITY_SERVICE", "Geofence removed.")
                    }
                    addOnFailureListener {
                        sendErrorNotification()
                    }
                }

                geofence.typeNetwork = typeNetwork
                geofence.defineRadius(this)
                geofence.defineRequest()

                geofenceClient.addGeofences(geofence.request, geofence.pendingIntent).run {
                    addOnSuccessListener {
                        Log.d("CONNECTIVITY_SERVICE", "Geofence added.")
                    }
                    addOnFailureListener {
                        sendErrorNotification()
                    }
                }
            } else {
                Log.e("WTF", "WTF.")
            }
        } else {
            Log.d("CONNECTIVITY_SERVICE", "Insignificant change.")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun handleSignificantConnectivityChange(context: Context): Pair<Boolean, String> {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)

        val currentTypeNetwork = when {
            activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                WI_FI_TYPE_CONNECTION
            }

            activeNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                CELLULAR_TYPE_CONNECTION
            }

            else -> {
                NO_CONNECTION_TYPE_CONNECTION
            }
        }

        return Pair(currentTypeNetwork != geofence.typeNetwork, currentTypeNetwork)
    }

    private fun sendErrorNotification() {
        manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = util.getConnectivityPendingIntent(this)
        val notification = util.getConnectivityNotification(CHANNEL_ID_CONNECTIVITY_NOTIFY, pendingIntent, this)

        getNotificationChannel()

        manager.notify(ID_CONNECTIVITY_NOTIFY, notification.build())
    }

    private fun getNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_CONNECTIVITY_NOTIFY,
            NAME_CHANNEL_CONNECTIVITY_NOTIFY,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            description = DESCRIPTION_CHANNEL_CONNECTIVITY_NOTIFY
        }

        manager.createNotificationChannel(channel)
    }
}