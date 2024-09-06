package com.example.brockapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.brockapp.interfaces.NetworkAvailableImpl
import com.example.brockapp.service.ConnectivityService
import com.example.brockapp.viewmodel.NetworkViewModel

private var lastHandledTime = 0L
private const val DEBOUNCE_INTERVAL = 5000L

class ConnectivityReceiver(private val viewModelStoreOwner: ViewModelStoreOwner): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastHandledTime < DEBOUNCE_INTERVAL) {
                return
            }
            lastHandledTime = currentTime

            if (networkInfo != null && networkInfo.isConnected) {
                when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        handleConnectivityChange(context)
                    }

                    ConnectivityManager.TYPE_MOBILE -> {
                        handleConnectivityChange(context)
                    }

                    else  -> {
                        Log.d("CONNECTIVITY_RECEIVER", "Connectivity change not recognized.")
                    }
                }
            } else {
                handleConnectivityChange(context)
            }
        } else {
            Log.d("CONNECTIVITY_RECEIVER", "Weird intent.")
        }
    }

    private fun handleConnectivityChange(context: Context) {
        val networkUtil = NetworkAvailableImpl()
        val networkViewModel = ViewModelProvider(viewModelStoreOwner)[NetworkViewModel::class.java]

        networkViewModel.setNetwork(networkUtil.isInternetActive(context))

        val serviceIntent = Intent(context, ConnectivityService::class.java)
        context.startService(serviceIntent)
    }
}