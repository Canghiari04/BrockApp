package com.example.brockapp.worker

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.singleton.MySupabase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.jan.supabase.postgrest.from

class DeleteGeofenceAreaWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val latitude = inputData.getDouble("LATITUDE", 0.0)
        val longitude = inputData.getDouble("LONGITUDE", 0.0)

        if (latitude != 0.0 && longitude != 0.0) {
            MySupabase.getInstance().from("GeofenceAreas").delete {
                filter { eq("latitude", latitude) }
                filter { eq("longitude", longitude) }
                filter { eq("username", MyUser.username) }
            }
        }

        return Result.success()
    }
}