package com.example.brockapp.worker

import com.example.brockapp.singleton.MySupabase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.brockapp.extraObject.MyUser
import io.github.jan.supabase.postgrest.from

class DeleteFromSupabaseWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val table = inputData.getString("TABLE")
        val id = inputData.getLong("MEMO_ID", 0L)

        if (!table.isNullOrEmpty() && id != 0L) {
            MySupabase.getInstance().from(table).delete {
                filter { eq("id", id) }
                filter { eq("username", MyUser.username) }
            }
        }

        return Result.success()
    }
}