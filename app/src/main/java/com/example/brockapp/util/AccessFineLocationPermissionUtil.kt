package com.example.brockapp.util

import com.example.brockapp.R

import android.net.Uri
import android.Manifest
import android.content.Intent
import android.app.AlertDialog
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale

class AccessFineLocationPermissionUtil(private val activity: AppCompatActivity, private val type: String, private val onPermissionGranted: (String) -> Unit) {

    private lateinit var requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>

    init {
        setUpLocationPermissionLauncher()
    }

    fun requestAccessLocationPermission() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setUpLocationPermissionLauncher() {
        requestLocationPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val areGranted = run {
                permissions.filter { !it.value }.keys.isEmpty()
            }

            when {
                areGranted -> {
                    onPermissionGranted(type)
                }

                shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rationale_permissions_title)
            .setMessage(R.string.rationale_location_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
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
            }
            .create()
            .show()
    }
}