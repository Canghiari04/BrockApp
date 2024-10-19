package com.example.brockapp.page.user

import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.page.ProgressPage
import com.example.brockapp.extraObject.MyUser

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

        groupViewModel.getCurrentFriends(MyUser.id)
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
        groupViewModel.errorAddFriend.observe(this) {
            if (it) {
                toastUtil.showBasicToast(
                    "${friend.username} is your new friend",
                    requireContext()
                )
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while adding",
                    requireContext()
                )
            }
        }
    }

    private fun observeRemovedFriend() {
        groupViewModel.errorDeleteFriend.observe(this) {
            if (it) {
                toastUtil.showBasicToast(
                    "${friend.username} has been removed",
                    requireContext()
                )
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while removing",
                    requireContext()
                )
            }
        }
    }

    private fun observeCurrentFriends() {
        groupViewModel.currentFriends.observe(viewLifecycleOwner) { items ->
            if (items.contains(friend.username)) {
                buttonUser.setText("REMOVE")
                buttonUser.setOnClickListener { groupViewModel.deleteFriend(friend.username) }
            } else {
                buttonUser.setOnClickListener { groupViewModel.addFriend(friend.username) }
            }
        }
    }

    override fun observeVehicleTimeSpent() {
        groupViewModel.friendVehicleTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.also {
                it.text = duration
            }
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
