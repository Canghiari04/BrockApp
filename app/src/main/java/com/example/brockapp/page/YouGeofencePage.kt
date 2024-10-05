package com.example.brockapp.page

class GeofencePage: BaseGeofencePage() {
    override fun loadGeofenceTransitions() {
        viewModel.getGeofenceTransitions()
    }
}