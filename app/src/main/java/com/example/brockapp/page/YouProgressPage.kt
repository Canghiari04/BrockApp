package com.example.brockapp.page

class YouProgressPage: ProgressPage() {
    override fun loadVehicleActivities(startOfPeriod: String, endOfPeriod: String) {
        viewModel.getVehicleActivities(startOfPeriod, endOfPeriod)
    }

    override fun loadKilometers(startOfPeriod: String, endOfPeriod: String) {
        viewModel.getKilometers(startOfPeriod, endOfPeriod)
    }

    override fun loadVehicleTime(startOfPeriod: String, endOfPeriod: String) {
        viewModel.getVehicleTime(startOfPeriod, endOfPeriod)
    }

    override fun loadWalkActivities(startOfPeriod: String, endOfPeriod: String){
        viewModel.getWalkActivities(startOfPeriod, endOfPeriod)
    }

    override fun loadWalkTime(startOfPeriod: String, endOfPeriod: String) {
        viewModel.getWalkTime(startOfPeriod, endOfPeriod)
    }

    override fun loadStepNumber(startOfPeriod: String, endOfPeriod: String) {
        viewModel.getSteps(startOfPeriod, endOfPeriod)
    }
}