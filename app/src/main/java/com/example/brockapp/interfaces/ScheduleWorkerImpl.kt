package com.example.brockapp.interfaces

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.worker.SyncBucketWorker

import androidx.work.Data
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder

class SchedulePeriodicWorkerImpl(private val context: Context): SchedulePeriodicWorker {
    override fun scheduleSyncPeriodic() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncBucketWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraint)
            .setInputData(
                Data.Builder()
                    .putString("USERNAME", MyUser.username)
                    .build()
            ).build()

        WorkManager.getInstance(context).also {
            it.pruneWork()
            it.enqueueUniquePeriodicWork(
                "SyncBucketWorker",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override fun deleteSyncPeriodic() {
        WorkManager.getInstance(context).cancelUniqueWork("SyncBucketWorker")
    }
}