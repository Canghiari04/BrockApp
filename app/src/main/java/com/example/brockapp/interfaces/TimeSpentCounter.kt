package com.example.brockapp.interfaces

import com.example.brockapp.data.UserActivity
import com.example.brockapp.database.UserStillActivityEntity

interface TimeSpentCounter {
    fun computeTimeSpent(userActivities: List<UserActivity>): Long

    fun computeTimeSpentStill(userStillActivities: List<UserStillActivityEntity>): Long
}