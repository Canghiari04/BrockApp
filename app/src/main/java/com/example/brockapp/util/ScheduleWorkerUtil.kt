package com.example.brockapp.util

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.worker.SyncBucketWorker
import com.example.brockapp.worker.DeleteMemoWorker
import com.example.brockapp.worker.DeleteActivityWorker
import com.example.brockapp.worker.DeleteGeofenceAreaWorker

import androidx.work.Data
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy

class ScheduleWorkerUtil(private val context: Context) {

    fun scheduleSyncPeriodic() {
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

    fun scheduleDeleteMemoWorker(id: Long) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putLong("ID", id)
            .build()

        val request = OneTimeWorkRequestBuilder<DeleteMemoWorker>()
            .setConstraints(constraint)
            .addTag("DeleteMemoWorker")
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun scheduleDeleteGeofenceAreaWorker(latitude: Double, longitude: Double) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putDouble("LATITUDE", latitude)
            .putDouble("LONGITUDE", longitude)
            .build()

        val request = OneTimeWorkRequestBuilder<DeleteGeofenceAreaWorker>()
            .setConstraints(constraint)
            .addTag("DeleteGeofenceAreaWorker")
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun scheduleDeleteActivityWorker(id: Long, table: String) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putLong("ID", id)
            .putString("TABLE", table)
            .build()

        val request = OneTimeWorkRequestBuilder<DeleteActivityWorker>()
            .setConstraints(constraint)
            .addTag("DeleteActivityWorker")
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun deleteSyncPeriodic() {
        WorkManager.getInstance(context).cancelUniqueWork("SyncBucketWorker")
    }

    fun deleteMemoWorker() {
        WorkManager.getInstance(context).cancelAllWorkByTag("DeleteMemoWorker")
    }

    fun deleteGeofenceAreaWorker() {
        WorkManager.getInstance(context).cancelAllWorkByTag("DeleteGeofenceAreaWorker")
    }

    fun deleteActivityWorker() {
        WorkManager.getInstance(context).cancelAllWorkByTag("DeleteActivityWorker")
    }
}