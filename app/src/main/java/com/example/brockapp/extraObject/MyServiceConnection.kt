package com.example.brockapp.extraObject

import com.example.brockapp.service.DistanceService
import com.example.brockapp.service.StepCounterService
import com.example.brockapp.service.HeightDifferenceService

import android.os.IBinder
import android.content.ComponentName
import android.content.ServiceConnection

object MyServiceConnection {
    fun createDistanceServiceConnection(onConnected: (DistanceService) -> Unit): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as DistanceService.LocalBinder
                onConnected(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                //
            }
        }
    }

    fun createStepCounterService(onConnected: (StepCounterService) -> Unit): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as StepCounterService.LocalBinder
                onConnected(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                //
            }
        }
    }

    fun createHeightDifferenceService(onConnected: (HeightDifferenceService) -> Unit): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as HeightDifferenceService.LocalBinder
                onConnected(binder.getService())
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                //
            }
        }
    }
}