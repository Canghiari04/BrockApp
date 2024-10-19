package com.example.brockapp.receiver

import com.example.brockapp.viewmodel.NetworkViewModel

import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

private var lastHandledTime = 0L
private const val DEBOUNCE_INTERVAL = 5000L

class AuthenticatorReceiver(private val viewModelStoreOwner: ViewModelStoreOwner): BroadcastReceiver() {
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
                handleChange(true)
            } else {
                handleChange(false)
            }
        }
    }

    private fun handleChange(isConnected: Boolean) {
        val networkViewModel = ViewModelProvider(viewModelStoreOwner)[NetworkViewModel::class.java]
        networkViewModel.setAuthNetwork(isConnected)
    }
}