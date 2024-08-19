package com.example.brockapp.util

import com.example.brockapp.R
import com.example.brockapp.activity.AuthenticatorActivity

import android.net.Uri
import android.Manifest
import android.widget.Toast
import android.content.Intent
import android.content.Context
import android.app.AlertDialog
import android.provider.Settings
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity

class PermissionUtil(private val context: Context, private val activity: FragmentActivity) {
    fun hasLocationPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasBackgroundPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowLocationPermissionsRationaleDialog(permissions: Array<String>): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    fun shouldShowBackgroundPermissionRationaleDialog(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun shouldShowNotificationPermissionRationaleDialog(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
    }
    
    /**
     * Metodo attuato per mostrare la finestra di dialogo successiva al "Deny" dei permessi
     * richiesti.
     */
    fun showPermissionsRationaleDialog() {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.settings_positive_button) { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(context, R.string.permissions_toast, Toast.LENGTH_LONG).show()
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                context.startActivity(Intent(context, AuthenticatorActivity::class.java))
            }
            .create()
            .show()
    }
}