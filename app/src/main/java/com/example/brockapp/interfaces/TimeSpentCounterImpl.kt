package com.example.brockapp.interfaces

import com.example.brockapp.*
import com.example.brockapp.data.UserActivity
import com.example.brockapp.database.UserStillActivityEntity

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeSpentCounterImpl: TimeSpentCounter {
    override fun computeTimeSpent(userActivities: List<UserActivity>): Long {
        var timeSpent = 0L
        val dateFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        for (i in userActivities.indices) {
            if (userActivities[i].transitionType == 1) {
                continue
            }

            val beginActivityTime = LocalDateTime.parse(userActivities[i].timestamp, dateFormatter)
            val nextActivity = if (i < userActivities.size - 1) userActivities[i + 1] else null

            if (nextActivity == null) {
                break
            }

            val endActivityTime = LocalDateTime.parse(nextActivity.timestamp, dateFormatter)
            val durationInSeconds = Duration.between(beginActivityTime, endActivityTime).seconds

            timeSpent += durationInSeconds
        }

        return timeSpent
    }

    override fun computeTimeSpentStill(userStillActivities: List<UserStillActivityEntity>): Long {
        var timeSpent = 0L
        val dateFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)

        for (i in userStillActivities.indices) {
            if (userStillActivities[i].transitionType == 1) {
                continue
            }

            val beginActivityTime = LocalDateTime.parse(userStillActivities[i].timestamp, dateFormatter)
            val nextActivity = if (i < userStillActivities.size - 1) userStillActivities[i + 1] else null

            if (nextActivity == null) {
                break
            }

            val endActivityTime = LocalDateTime.parse(nextActivity.timestamp, dateFormatter)
            val durationInSeconds = Duration.between(beginActivityTime, endActivityTime).seconds

            timeSpent += durationInSeconds
        }

        return timeSpent
    }
}