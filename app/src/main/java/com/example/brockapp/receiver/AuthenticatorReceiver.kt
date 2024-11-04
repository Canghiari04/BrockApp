package com.example.brockapp.receiver

import com.example.brockapp.worker.ConnectivityWorker
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.interfaces.InternetAvailableImpl

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

class AuthenticatorReceiver(private val viewModelStoreOwner: ViewModelStoreOwner): BroadcastReceiver() {
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
                it.setAuthNetwork(isConnected)
            }

            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<ConnectivityWorker>()
                    .setInputData(
                        Data.Builder()
                            .putBoolean("IS_CONNECTED", isConnected)
                            .build()
                    ).build()
            )
        }
    }
}