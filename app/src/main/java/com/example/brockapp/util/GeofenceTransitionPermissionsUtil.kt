package com.example.brockapp.util

import com.example.brockapp.R

import android.net.Uri
import android.Manifest
import android.content.Intent
import android.app.AlertDialog
import android.provider.Settings
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale

class GeofenceTransitionPermissionsUtil(private val activity: AppCompatActivity, private val onPermissionsGranted: () -> Unit, private val switch: SwitchCompat) {

    private lateinit var requestBackgroundPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestLocationPermissionsLauncher: ActivityResultLauncher<Array<String>>
    
    init {
        setUpGeofenceTransitionLauncher()
    }

    fun requestGeofenceTransitionPermissions() {
        requestLocationPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun setUpGeofenceTransitionLauncher() {
        requestLocationPermissionsLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val areGranted = run {
                val deniedPermissions = permissions.filter { !it.value }.keys
                deniedPermissions.isEmpty()
            }

            when {
                areGranted -> {
                    requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }

                shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showAccessLocationPermissionRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }

        requestBackgroundPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> {
                    onPermissionsGranted()
                    switch.isChecked = true
                }

                shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                    showBackgroundLocationPermissionRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showAccessLocationPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rationale_permissions_title)
            .setMessage(R.string.rationale_location_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestLocationPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
                switch.isChecked = false
            }
            .create()
            .show()
    }

    private fun showBackgroundLocationPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rationale_permission_title)
            .setMessage(R.string.rationale_background_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestBackgroundPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
                switch.isChecked = false
            }
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.setting_permission_title)
            .setMessage(R.string.settings_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                activity.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", activity.packageName, null)
                    )
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
                switch.isChecked = false
            }
            .create()
            .show()
    }
}