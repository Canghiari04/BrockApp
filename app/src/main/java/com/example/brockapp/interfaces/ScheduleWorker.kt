package com.example.brockapp.interfaces

interface ScheduleWorker {
    fun scheduleDeleteGeofenceAreaWorker(latitude: Double, longitude: Double)

    fun scheduleDeleteMemoWorker(id: Long)

    fun scheduleSyncPeriodic()

    fun deleteGeofenceAreaWorker()

    fun deleteMemoWorker()

    fun deleteSyncPeriodic()
}