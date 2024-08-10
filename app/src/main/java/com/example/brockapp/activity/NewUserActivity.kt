package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.fragment.PageLoaderActivityFragment
import com.example.brockapp.detect.UserActivityBroadcastReceiver
import com.example.brockapp.REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION

import android.util.Log
import android.Manifest
import android.os.Bundle
import android.content.Intent
import android.app.AlertDialog
import android.content.IntentFilter
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NewUserActivity: AppCompatActivity() {
    companion object {
        val userActivityBroadcastReceiver = UserActivityBroadcastReceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkActivityPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION) {
            when {
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    showNewActivityPage()
                    registerActivityRecognition()
                }
                else -> {
                    showDetectPermissionDialog()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(userActivityBroadcastReceiver)
        } catch (e: Exception) {
            Log.d("BROADCAST_RECEIVER", e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(userActivityBroadcastReceiver)
        } catch (e: Exception) {
            Log.d("BROADCAST_RECEIVER", e.toString())
        }
    }

    /**
     * Metodo attuato per definire se il permesso di activity recognition sia stato accettato
     * oppure negato.
     */
    private fun checkActivityPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            showNewActivityPage()
            registerActivityRecognition()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            showDetectPermissionDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION
            )
        }
    }

    /**
     * Metodo richiamato quando i permessi sono accettati. Imposta il corretto fragment all'interno
     * del frame layout dell'activity.
     */
    private fun showNewActivityPage() {
        setContentView(R.layout.new_user_activity)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.detect_fragment, PageLoaderActivityFragment())
            commit()
        }
    }

    /**
     * Metodo attuato per registrare il broadcast receiver, affinchè possa ricevere updates relativi
     * ad activity recognition.
     */
    private fun registerActivityRecognition() {
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(userActivityBroadcastReceiver, IntentFilter("TRANSITIONS_RECEIVER_ACTION"))
        } catch (e: Exception) {
            Log.d("BROADCAST RECEIVER", userActivityBroadcastReceiver.toString())
        }
    }

    /**
     * Metodo attuato per mostrare la finestra di dialogo necessaria per accettare i permessi
     * richiesti.
     */
    private fun showDetectPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
                finish()
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, PageLoaderActivity::class.java).putExtra("TYPE_PAGE", "HOME"))
                finish()
            }
            .create()
            .show()
    }
}