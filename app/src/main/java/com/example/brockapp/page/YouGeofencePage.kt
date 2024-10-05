package com.example.brockapp.page

class YouGeofencePage: GeofencePage() {
    override fun loadGeofenceTransitions() {
        viewModel.getGeofenceTransitions()
    }
}