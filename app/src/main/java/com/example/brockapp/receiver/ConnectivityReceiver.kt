package com.example.brockapp.receiver

import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.service.ConnectivityService
import com.example.brockapp.interfaces.NetworkAvailableImpl

import android.util.Log
import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class ConnectivityReceiver(private val viewModelStoreOwner: ViewModelStoreOwner): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val networkUtil = NetworkAvailableImpl()
            val networkViewModel = ViewModelProvider(viewModelStoreOwner)[NetworkViewModel::class.java]

            networkViewModel.setNetwork(networkUtil.isInternetActive(context))

            val serviceIntent = Intent(context, ConnectivityService::class.java)
            context.startService(serviceIntent)
        } else {
            Log.d("CONNECTIVITY_RECEIVER", "Weird intent.")
        }
    }
}