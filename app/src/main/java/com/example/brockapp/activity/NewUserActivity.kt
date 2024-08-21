package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE
import com.example.brockapp.receiver.ActivityRecognitionReceiver
import com.example.brockapp.REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION

import android.net.Uri
import android.Manifest
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.app.AlertDialog
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NewUserActivity : AppCompatActivity() {
    private lateinit var receiver: ActivityRecognitionReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_user_activity)

        receiver = ActivityRecognitionReceiver()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_user_activity)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkActivityPermission()

        findViewById<Button>(R.id.button_still).setOnClickListener {
            startActivity(Intent(this, StillActivity::class.java))
        }

        findViewById<Button>(R.id.button_vehicle).setOnClickListener {
            startActivity(Intent(this, VehicleActivity::class.java))
        }


        findViewById<Button>(R.id.button_walk).setOnClickListener {
            startActivity(Intent(this, WalkActivity::class.java))
        }

        findViewById<Button>(R.id.button_run_activity).setOnClickListener {

        }
    }

    override fun onResume() {
        super.onResume()
        receiver = ActivityRecognitionReceiver()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION) {
            when {
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    registerActivityRecognitionReceiver()
                }
                else -> {
                    showDetectPermissionDialog()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home"))
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Metodo attuato per definire se il permesso di activity recognition sia stato accettato
     * oppure negato.
     */
    private fun checkActivityPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            registerActivityRecognitionReceiver()
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
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home"))
            }
            .create()
            .show()
    }

    /**
     * Metodo attuato per registrare il broadcast receiver, affinchè possa ricevere updates relativi
     * ad activity recognition.
     */
    private fun registerActivityRecognitionReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT_TYPE))
    }
}