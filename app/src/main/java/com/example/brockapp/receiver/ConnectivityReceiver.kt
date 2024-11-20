package com.example.brockapp.receiver

import com.example.brockapp.service.GeofenceService
import com.example.brockapp.viewModel.NetworkViewModel
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.worker.ConnectivityNotifierWorker

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

            ViewModelProvider(viewModelStoreOwner)[NetworkViewModel::class.java].also {
                it.setNetwork(isConnected)
            }

            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<ConnectivityNotifierWorker>()
                    .setInputData(
                        Data.Builder()
                            .putBoolean("IS_CONNECTED", isConnected)
                            .build()
                    )
                    .build()
            )

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