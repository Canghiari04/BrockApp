package com.example.brockapp.worker

import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.singleton.MySupabase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.jan.supabase.postgrest.from

class DeleteActivityWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong("ID", 0L)
        val table = inputData.getString("TABLE")

        if (id != 0L) {
            MySupabase.getInstance().from(table!!).delete {
                filter { eq("id", id) }
                filter { eq( "username", MyUser.username) }
            }
        }

        return Result.success()
    }
}