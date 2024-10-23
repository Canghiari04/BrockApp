package com.example.brockapp.page.you

import com.example.brockapp.page.ProgressPage

import android.view.View
import kotlin.time.Duration.Companion.milliseconds

class YouProgressPage: ProgressPage() {
    companion object {
        const val TO_KM = 1000.0
    }

    override fun setUpCardView() {
        cardViewUserProgressPage.visibility = View.GONE
        cardViewYouProgressPage.visibility = View.VISIBLE
    }

    override fun observeVehicleTimeSpent() {
        viewModelActivities.vehicleTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeUserKilometersTravelled() {
        viewModelActivities.metersTravelled.observe(viewLifecycleOwner) {
            val kilometers = (it / TO_KM)

            titleSecondColumn.setText("Distance travelled")
            infoSecondColumn.text = ("%.1f km".format(kilometers))
        }
    }

    override fun observeVehicleBarChartEntries() {
        viewModelActivities.vehicleBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(vehicleBarChart, entries, requireContext())
            }
        }
    }

    override fun observeRunTimeSpent() {
        viewModelActivities.runTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeUserRunDistanceDone() {
        viewModelActivities.metersRun.observe(viewLifecycleOwner) {
            val kilometers = (it / TO_KM)

            titleSecondColumn.setText("Distance done")
            infoSecondColumn.text = ("%.3f km".format(kilometers))
        }
    }

    override fun observeRunBarChartEntries() {
        viewModelActivities.runBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(runBarChart, entries, requireContext())
            }
        }
    }

    override fun observeStillTimeSpent() {
        viewModelActivities.stillTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeStillBarChartEntries() {
        viewModelActivities.stillBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(stillBarChart, entries, requireContext())
            }
        }
    }

    override fun observeWalkTimeSpent() {
        viewModelActivities.walkTime.observe(viewLifecycleOwner) {
            val duration = it.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            infoFirstColumn.setText(duration)
        }
    }

    override fun observeUserSteps() {
        viewModelActivities.steps.observe(viewLifecycleOwner) { item ->
            titleSecondColumn.setText("Steps")
            infoSecondColumn.setText(item.toString() + " steps")
        }
    }

    override fun observeWalkBarChartEntries() {
        viewModelActivities.walkBarChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateBarChart(walkBarChart, entries, requireContext())
            }
        }
    }

    override fun observeVehicleLineChartEntries() {
        viewModelActivities.vehicleLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Distance traveled", vehicleLineChart, entries)
            }
        }
    }

    override fun observeRunLineChartEntries() {
        viewModelActivities.runLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Kilometers done", runLineChart, entries)
            }
        }
    }

    override fun observeWalkLineChartEntries() {
        viewModelActivities.walkLineChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populateLineChart("Steps done", walkLineChart, entries)
            }
        }
    }

    override fun observeUserActivities() {
        viewModelActivities.pieChartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNotEmpty()) {
                chartUtil.populatePieChart(entries, pieChart, requireContext())
            }
        }
    }

    override fun loadVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getVehicleTime(startOfPeriod, endOfPeriod)
    }

    override fun loadKilometersTravelled(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getKilometers(startOfPeriod, endOfPeriod)
    }

    override fun defineVehicleBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getVehicleBarChartEntries(startOfWeek, endOfWeek)
    }

    override fun loadRunTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getRunTime(startOfPeriod, endOfPeriod)
    }

    override fun loadRunDistanceDone(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getKilometersRun(startOfPeriod, endOfPeriod)
    }

    override fun defineRunBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getRunBarChartEntries(startOfWeek, endOfWeek)
    }

    override fun loadStillTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getStillTime(startOfPeriod, endOfPeriod)
    }

    override fun defineStillBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getStillBarChartEntries(startOfWeek, endOfWeek)
    }

    override fun loadWalkTime(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getWalkTime(startOfPeriod, endOfPeriod)
    }

    override fun loadStepNumber(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getSteps(startOfPeriod, endOfPeriod)
    }

    override fun defineWalkBarChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getWalkBarChartEntries(startOfWeek, endOfWeek)
    }

    override fun defineVehicleLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getVehicleLineChartEntries(startOfWeek, endOfWeek)
    }

    override fun defineRunLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getRunLineChartEntries(startOfWeek, endOfWeek)
    }

    override fun defineWalkLineChartEntries(startOfWeek: String, endOfWeek: String) {
        viewModelActivities.getWalkLineChartEntries(startOfWeek, endOfWeek)
    }

    override fun countActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModelActivities.getCountOfActivities(startOfPeriod, endOfPeriod)
    }
}