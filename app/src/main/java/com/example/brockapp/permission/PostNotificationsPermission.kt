package com.example.brockapp.permission

import com.example.brockapp.R

import android.net.Uri
import android.Manifest
import android.os.Build
import android.content.Intent
import android.app.AlertDialog
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class PostNotificationsPermission(private val activity: FragmentActivity, private val onPermissionGranted: () -> Unit) {
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    init {
        setUpLauncher()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPostNotificationPermission() {
        requestNotificationPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setUpLauncher() {
        requestNotificationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> {
                    onPermissionGranted()
                }

                shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPostNotificationsPermissionRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPostNotificationsPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rationale_permission_title)
            .setMessage(R.string.rationale_notifications_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
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