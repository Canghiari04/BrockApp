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

class AccountActivityPermissionUtil(private val activity: AppCompatActivity, private val onPermissionGranted: () -> Unit) {
    private lateinit var requestReadStoragePermissionLauncher: ActivityResultLauncher<String>

    init {
        setUpReadStorageLauncher()
    }

    fun requestReadStoragePermission() {
        requestReadStoragePermissionLauncher.launch(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun setUpReadStorageLauncher() {
        requestReadStoragePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> {
                    onPermissionGranted()
                }

                shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showReadStoragePermissionRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showReadStoragePermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rationale_permission_title)
            .setMessage(R.string.rationale_read_storage_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestReadStoragePermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
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