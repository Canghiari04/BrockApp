package com.example.brockapp.service

import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.WI_FI_TYPE_CONNECTION
import com.example.brockapp.CELLULAR_TYPE_CONNECTION
import com.example.brockapp.NO_CONNECTION_TYPE_CONNECTION

import android.util.Log
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import android.content.IntentFilter
import kotlinx.coroutines.Dispatchers
import android.net.NetworkCapabilities
import android.net.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import android.content.BroadcastReceiver
import com.google.android.gms.location.LocationServices

class ConnectivityService: Service() {
    private lateinit var db: BrockDB
    private lateinit var geofence: MyGeofence

    override fun onCreate() {
        super.onCreate()

        db = BrockDB.getInstance(this)
        geofence = MyGeofence.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val (significantChange, typeNetwork) = handleSignificantConnectivityChange(this)

        if (significantChange) {
//        TODO -> Gestione dei fences a seconda della tipologia di rete presente.
//        val geofenceClient = LocationServices.getGeofencingClient(this)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            geofence.typeNetwork = typeNetwork
//
//            val areas = db.GeofenceAreaDao().getAllGeofenceAreas()
//
//            if (areas.isEmpty()) {
//                Log.d("WTF", "WTF")
//            } else {
//                geofenceClient.removeGeofences(geofence.pendingIntent).run {
//                    addOnSuccessListener {
//                        Log.d("CONNECTIVITY_SERVICE", "Geofence removed.")
//                        geofence.init(context, areas)
//                    }
//                    addOnFailureListener {
//                        Log.d("CONNECTIVITY_SERVICE", "Geofence not removed.")
//                    }
//                }
//            }
//        }
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
}