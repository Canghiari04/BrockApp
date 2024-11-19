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

class ActivityRecognitionPermissionUtil(private val activity: AppCompatActivity, private val onPermissionGranted: () -> Unit, private val switch: SwitchCompat?) {

    private lateinit var requestRecognitionPermissionLauncher: ActivityResultLauncher<String>

    init {
        setUpActivityRecognitionLauncher()
    }

    fun requestActivityRecognitionPermission() {
        requestRecognitionPermissionLauncher.launch(
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }

    private fun setUpActivityRecognitionLauncher() {
        requestRecognitionPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> {
                    onPermissionGranted()
                }

                shouldShowRequestPermissionRationale(activity, Manifest.permission.ACTIVITY_RECOGNITION) -> {
                    showActivityRecognitionPermissionRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showActivityRecognitionPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rationale_permission_title)
            .setMessage(R.string.rationale_recognition_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestRecognitionPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
                switch?.let { it.isChecked = false }
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
                switch?.let { it.isChecked = false }
            }
            .create()
            .show()
    }
}