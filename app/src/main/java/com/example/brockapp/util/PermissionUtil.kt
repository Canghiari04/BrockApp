package com.example.brockapp.util

import com.example.brockapp.R
import com.example.brockapp.activity.AuthenticatorActivity

import android.net.Uri
import android.Manifest
import android.content.Intent
import android.app.AlertDialog
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale

class PermissionUtil(private val activity: FragmentActivity, private val onPermissionGranted: () -> Unit) {
    private lateinit var requestLocationPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestBackgroundPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    init {
        setupLaunchers()
    }

    private fun setupLaunchers() {
        requestLocationPermissionsLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }.keys

            if (deniedPermissions.isEmpty()) {
                requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else if (shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionsRationaleDialog()
            } else {
                showLocationPermissionsDeniedDialog()
            }
        }

        requestBackgroundPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                showPermissionsRationaleDialog()
            } else {
                showBackgroundPermissionDeniedDialog()
            }
        }

        requestNotificationPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                onPermissionGranted()
            } else if (shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionsRationaleDialog()
            } else {
                showNotificationPermissionDeniedDialog()
            }
        }
    }

    fun requestPermissions() {
        requestLocationPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Metodo attuato per mostrare la finestra di dialogo successiva al "Deny" dei permessi
     * richiesti.
     */
    fun showPermissionsRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.settings_positive_button) { dialog, _ ->
                dialog.dismiss()
                activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null)))
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                activity.startActivity(Intent(activity, AuthenticatorActivity::class.java))
            }
            .create()
            .show()
    }

    /**
     * Metodo attuato per mostrare la finestra di dialogo successiva al "Deny" dei permessi
     * richiesti.
     */
    private fun showLocationPermissionsDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_location)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                requestLocationPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showBackgroundPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_background)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                requestBackgroundPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_notification)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}