package com.example.brockapp.service

import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyGeofence

import android.util.Log
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import android.content.IntentFilter
import kotlinx.coroutines.Dispatchers
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import com.example.brockapp.CELLULAR_TYPE_CONNECTION
import com.example.brockapp.NO_CONNECTION_TYPE_CONNECTION
import com.example.brockapp.WI_FI_TYPE_CONNECTION
import kotlinx.coroutines.CoroutineScope
import com.google.android.gms.location.LocationServices

class ConnectivityService: Service() {
    private lateinit var db: BrockDB
    private lateinit var geofence: MyGeofence
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    geofence = MyGeofence.getInstance()

                    if (handleSignificantConnectivityChange(context)) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db = BrockDB.getInstance(context)
                            val areas = db.GeofenceAreaDao().getAllGeofenceAreas()

                            if (areas.isEmpty()) {
                                Log.d("WTF", "WTF")
                            } else {
                                val geofenceClient = LocationServices.getGeofencingClient(context)
                                geofenceClient.removeGeofences(geofence.pendingIntent).run {
                                    addOnSuccessListener {
                                        Log.d(
                                            "CONNECTIVITY_SERVICE",
                                            "Cancellazione andata a buon fine."
                                        )
                                        geofence.init(context, areas)
                                    }
                                    addOnFailureListener {
                                        Log.d("CONNECTIVITY_SERVICE", "Azione non conclusa.")
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("CONNECTIVITY_SERVICE", "Cambiamento insignificante.")
                    }
                }
            }
        }

        registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun handleSignificantConnectivityChange(context: Context): Boolean {
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

        if (currentTypeNetwork != geofence.typeNetwork){
            geofence.typeNetwork = currentTypeNetwork
            return true
        }  else{
            return false
        }
    }
}