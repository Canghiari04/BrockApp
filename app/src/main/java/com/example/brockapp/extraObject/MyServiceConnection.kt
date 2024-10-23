package com.example.brockapp.extraObject

import com.example.brockapp.service.DistanceService
import com.example.brockapp.service.StepCounterService
import com.example.brockapp.service.HeightDifferenceService

import android.os.IBinder
import android.content.ComponentName
import android.content.ServiceConnection

object MyServiceConnection {
    // I passed a callback inside the function parameters, so I can get the service instance
    fun createDistanceServiceConnection(
        onConnected: (DistanceService) -> Unit,
        onDisconnected: () -> Unit
    ): ServiceConnection {
        return object : ServiceConnection {
            // When the binding is completed I will obtain the instance by the binder
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as DistanceService.LocalBinder
                onConnected(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                onDisconnected()
            }
        }
    }

    fun createStepCounterService(
        onConnected: (StepCounterService) -> Unit,
        onDisconnected: () -> Unit
    ): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as StepCounterService.LocalBinder
                onConnected(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                onDisconnected()
            }
        }
    }

    fun createHeightDifferenceService(
        onConnected: (HeightDifferenceService) -> Unit,
        onDisconnected: () -> Unit
    ): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as HeightDifferenceService.LocalBinder
                onConnected(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                onDisconnected()
            }
        }
    }
}