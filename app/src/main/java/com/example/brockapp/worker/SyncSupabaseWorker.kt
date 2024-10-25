package com.example.brockapp.worker

import com.example.brockapp.room.BrockDB
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.ActivitiesViewModel

import androidx.work.Worker
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkerParameters
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.UserViewModelFactory
import java.io.File

class SyncSupabase(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private lateinit var db: BrockDB

    override fun doWork(): Result {
        db = BrockDB.getInstance(context)

        return Result.success()
    }
}