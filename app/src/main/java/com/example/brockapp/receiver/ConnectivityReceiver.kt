package com.example.brockapp.receiver

import com.example.brockapp.service.GeofenceService
import com.example.brockapp.worker.ConnectivityWorker
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl

import android.util.Log
import androidx.work.Data
import android.content.Intent
import android.content.Context
import androidx.work.WorkManager
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.work.OneTimeWorkRequestBuilder

private var lastHandledTime = 0L
private const val DEBOUNCE_INTERVAL = 5000L

class ConnectivityReceiver(private val viewModelStoreOwner: ViewModelStoreOwner): BroadcastReceiver() {
    private var networkUtil = InternetAvailableImpl()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastHandledTime < DEBOUNCE_INTERVAL) {
                return
            }
            lastHandledTime = currentTime

            val isConnected = networkUtil.isInternetActive(context)

            // Updating the state of network to able or disable some features
            ViewModelProvider(viewModelStoreOwner)[NetworkViewModel::class.java].also {
                it.setNetwork(isConnected)
            }

            // Define a worker to send a notification if the state of network is changed
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<ConnectivityWorker>()
                    .setInputData(
                        Data.Builder()
                            .putBoolean("IS_CONNECTED", isConnected)
                            .build()
                    )
                    .build()
            )

            // Define new radius for geofence monitoring
            Intent(context, GeofenceService::class.java).also {
                it.action = GeofenceService.Actions.RESTART.toString()

                if (MySharedPreferences.checkService("GEOFENCE_TRANSITION", context)) {
                    context.startService(it)
                }
            }
        } else {
            Log.d("CONNECTIVITY_RECEIVER", "Intent action weird")
        }
    }
}