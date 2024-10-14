package com.example.brockapp.page.friend

import com.example.brockapp.data.Friend
import com.example.brockapp.page.ProgressPage

import android.view.View
import androidx.cardview.widget.CardView
import kotlin.time.Duration.Companion.milliseconds

class FriendProgressPage(private val friend: Friend): ProgressPage() {
    companion object {
        const val TO_KM = 1000.0
    }

    override fun showWelcomeCardView(cardView: CardView) {
        cardView.visibility = View.GONE
    }

    override fun observeVehicleTimeSpent() {
        groupViewModel.friendVehicleTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeUserKilometers() {
        groupViewModel.friendMeters.observe(viewLifecycleOwner) {
            val kilometers = (it / TO_KM)
            infoSecondColumn.text = ("%.1f km".format(kilometers))
        }
    }

    override fun observeVehicleBarChartEntries() {
        groupViewModel.friendVehicleBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(vehicleBarChart, entries, requireContext())
            }
        }
    }

    override fun observeStillTimeSpent() {
        groupViewModel.friendStillTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeStillBarChartEntries() {
        groupViewModel.friendStillBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(stillBarChart, entries, requireContext())
            }
        }
    }

    override fun observeWalkTimeSpent() {
        groupViewModel.friendWalkTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeUserSteps() {
        groupViewModel.friendSteps.observe(viewLifecycleOwner) { item ->
            titleSecondColumn.setText("Steps")
            infoSecondColumn.setText(item.toString() + " steps")
        }
    }

    override fun observeWalkBarChartEntries() {
        groupViewModel.friendWalkBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(walkBarChart, entries, requireContext())
            }
        }
    }

    override fun observeVehicleLineChartEntries() {
        groupViewModel.friendVehicleLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Distance traveled", vehicleLineChart, entries)
            }
        }
    }

    override fun observeWalkLineChartEntries() {
        groupViewModel.friendWalkLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Steps done", vehicleLineChart, entries)
            }
        }
    }

    override fun observeUserActivities() {
        groupViewModel.friendPieChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populatePieChart(entries, pieChart, requireContext())
            }
        }
    }

    override fun loadVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        groupViewModel.getFriendVehicleTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun loadKilometers(startOfPeriod: String, endOfPeriod: String) {
        groupViewModel.getFriendKilometers(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineVehicleBarChartEntries(startOfWeek: String, endOfWeek: String) {
        groupViewModel.getFriendVehicleBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun loadStillTime(startOfPeriod: String, endOfPeriod: String) {
        groupViewModel.getFriendStillTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineStillBarChartEntries(startOfWeek: String, endOfWeek: String) {
        groupViewModel.getFriendStillBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun loadWalkTime(startOfPeriod: String, endOfPeriod: String) {
        groupViewModel.getFriendWalkTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun loadStepNumber(startOfPeriod: String, endOfPeriod: String) {
        groupViewModel.getFriendSteps(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineWalkBarChartEntries(startOfWeek: String, endOfWeek: String) {
        groupViewModel.getFriendWalkBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun defineVehicleLineChartEntries(startOfWeek: String, endOfWeek: String) {
        groupViewModel.getFriendVehicleLineChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun defineWalkLineChartEntries(startOfWeek: String, endOfWeek: String) {
        groupViewModel.getFriendWalkLineChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun countActivities(startOfPeriod: String, endOfPeriod: String) {
        groupViewModel.getFriendCountOfActivities(startOfPeriod, endOfPeriod, friend)
    }
}
