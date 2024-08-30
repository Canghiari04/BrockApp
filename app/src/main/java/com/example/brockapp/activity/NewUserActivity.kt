package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R

import android.net.Uri
import android.Manifest
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.app.AlertDialog
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity

class NewUserActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        checkActivityPermission()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_user_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Button>(R.id.button_still).setOnClickListener {
            val intent = Intent(this, StillActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.button_vehicle).setOnClickListener {
            val intent = Intent(this, VehicleActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.button_walk).setOnClickListener {
            val intent = Intent(this, WalkActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home")
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    /**
     * Metodo attuato per definire se il permesso di activity recognition sia stato accettato
     * oppure negato.
     */
    private fun checkActivityPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            return
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            showPermissionDialog()
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
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home")
                startActivity(intent)
                finish()
            }
            .create()
            .show()
    }
}