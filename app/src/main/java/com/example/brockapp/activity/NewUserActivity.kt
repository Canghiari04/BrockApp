package com.example.brockapp.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.example.brockapp.R
import com.example.brockapp.fragment.PageLoaderActivityFragment
import androidx.core.content.ContextCompat.registerReceiver
import com.example.brockapp.detect.UserActivityBroadcastReceiver

class NewUserActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_ACTIVITY_RECOGNITION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(startDetectActivity()) {
            registerActivityRecognition()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE_ACTIVITY_RECOGNITION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNewActivityPage()
                registerActivityRecognition()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(UserActivityBroadcastReceiver())
    }

    /**
     * Controllo se il permesso ACTIVITY_RECOGNITION è stato dato.
     */
    private fun startDetectActivity(): Boolean {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            showNewActivityPage()
            return true
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            showPermissionDialog()
            return false
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE_ACTIVITY_RECOGNITION
            )
            return false
        }
    }

    private fun registerActivityRecognition() {
        val intentFilter = IntentFilter("TRANSITIONS_RECEIVER_ACTION")

        registerReceiver(UserActivityBroadcastReceiver(), intentFilter, RECEIVER_NOT_EXPORTED)
    }

    private fun showNewActivityPage() {
        setContentView(R.layout.new_user_activity)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.detect_fragment, PageLoaderActivityFragment())
            commit()
        }
    }

    /** 
     * Visualizzazione della finestra di dialogo per ottenere il permesso.
     */
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_CODE_ACTIVITY_RECOGNITION
                )
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, PageLoaderActivity::class.java).putExtra("TYPE_PAGE", "HOME"))
            }
            .create()
            .show()
    }
}
