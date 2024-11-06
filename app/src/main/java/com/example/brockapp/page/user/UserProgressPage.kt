package com.example.brockapp.page.user

import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.page.ProgressPage

import android.view.View
import android.widget.TextView
import kotlin.time.Duration.Companion.milliseconds

class UserProgressPage(private val friend: Friend): ProgressPage() {
    companion object {
        const val TO_KM = 1000.0
    }

    override fun setUpCardView() {
        cardViewYouProgressPage.visibility = View.GONE
        cardViewUserProgressPage.visibility = View.VISIBLE

        setUpTextView()

        observeAddedFriend()
        observeRemovedFriend()
        observeCurrentFriends()

        viewModelGroup.getCurrentFriends()
    }

    private fun setUpTextView() {
        requireView().findViewById<TextView>(R.id.text_view_user_user_name).also {
            it.text = friend.username
        }

        requireView().findViewById<TextView>(R.id.text_view_user_address).also {
            it.text = "${friend.country}, ${friend.city}"
        }
    }

    private fun observeAddedFriend() {
        viewModelGroup.errorAddFriend.observe(this) {
            if (it) {
                toastUtil.showBasicToast(
                    "${friend.username} is your new friend",
                    requireContext()
                )

                viewModelGroup.getCurrentFriends()
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while adding",
                    requireContext()
                )
            }
        }
    }

    private fun observeRemovedFriend() {
        viewModelGroup.errorDeleteFriend.observe(this) {
            if (it) {
                toastUtil.showBasicToast(
                    "${friend.username} has been removed",
                    requireContext()
                )

                viewModelGroup.getCurrentFriends()
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while removing",
                    requireContext()
                )
            }
        }
    }

    private fun observeCurrentFriends() {
        viewModelGroup.currentFriends.observe(viewLifecycleOwner) { items ->
            if (items.contains(friend.username)) {
                buttonUser.setText("REMOVE")
                buttonUser.setOnClickListener { viewModelGroup.deleteFriend(friend.username) }
            } else {
                buttonUser.setText("ADD")
                buttonUser.setOnClickListener { viewModelGroup.addFriend(friend.username) }
            }
        }
    }

    override fun observeVehicleTimeSpent() {
        viewModelGroup.userVehicleTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.also {
                it.text = duration
            }
        }
    }

    override fun observeUserKilometersTravelled() {
        viewModelGroup.userMetersTravelled.observe(viewLifecycleOwner) {
            val kilometers = (it / TO_KM)

            titleSecondColumn.setText("Distance travelled")
            infoSecondColumn.text = ("%.1f km".format(kilometers))
        }
    }

    override fun observeVehicleBarChartEntries() {
        viewModelGroup.userVehicleBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(vehicleBarChart, entries, requireContext())
            }
        }
    }

    override fun observeRunTimeSpent() {
        viewModelGroup.userRunTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.also {
                it.text = duration
            }
        }
    }

    override fun observeUserRunDistanceDone() {
        viewModelGroup.userMetersRun.observe(viewLifecycleOwner) {
            val kilometers = (it / TO_KM)

            titleSecondColumn.setText("Distance done")
            infoSecondColumn.text = ("%.1f km".format(kilometers))
        }
    }

    override fun observeRunBarChartEntries() {
        viewModelGroup.userRunBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(runBarChart, entries, requireContext())
            }
        }
    }

    override fun observeStillTimeSpent() {
        viewModelGroup.userStillTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeStillBarChartEntries() {
        viewModelGroup.userStillBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(stillBarChart, entries, requireContext())
            }
        }
    }

    override fun observeWalkTimeSpent() {
        viewModelGroup.userWalkTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeUserSteps() {
        viewModelGroup.userSteps.observe(viewLifecycleOwner) { item ->
            titleSecondColumn.setText("Steps")
            infoSecondColumn.setText(item.toString() + " steps")
        }
    }

    override fun observeWalkBarChartEntries() {
        viewModelGroup.userWalkBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(walkBarChart, entries, requireContext())
            }
        }
    }

    override fun observeVehicleLineChartEntries() {
        viewModelGroup.userVehicleLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Distance travelled", vehicleLineChart, entries)
            }
        }
    }

    override fun observeRunLineChartEntries() {
        viewModelGroup.userRunLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Distance done", runLineChart, entries)
            }
        }
    }

    override fun observeWalkLineChartEntries() {
        viewModelGroup.userWalkLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Steps done", walkLineChart, entries)
            }
        }
    }

    override fun observeUserActivities() {
        viewModelGroup.userPieChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populatePieChart(entries, pieChart, requireContext())
            }
        }
    }

    override fun loadVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserVehicleTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun loadKilometersTravelled(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserKilometersTravelled(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineVehicleBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserVehicleBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun loadRunTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserRunTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun loadRunDistanceDone(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserKilometersRun(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineRunBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserRunBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun loadStillTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserStillTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineStillBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserStillBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun loadWalkTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserWalkTime(startOfPeriod, endOfPeriod, friend)
    }

    override fun loadStepNumber(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserSteps(startOfPeriod, endOfPeriod, friend)
    }

    override fun defineWalkBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserWalkBarChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun defineVehicleLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserVehicleLineChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun defineRunLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserRunLineChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun defineWalkLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelGroup.getUserWalkLineChartEntries(startOfWeek, endOfWeek, friend)
    }

    override fun countActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelGroup.getUserCountOfActivities(startOfPeriod, endOfPeriod, friend)
    }
}
