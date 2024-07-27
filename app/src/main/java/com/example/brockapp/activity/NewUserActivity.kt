package com.example.brockapp.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.example.brockapp.PageLoaderActivity
import com.example.brockapp.R
import com.example.brockapp.fragment.StillFragment
import com.example.brockapp.fragment.WalkFragment
import com.example.brockapp.fragment.VehicleFragment

class NewUserActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_ACTIVITY_RECOGNITION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askDetectPermission()


        // Imposta il contenuto della vista
        setContentView(R.layout.new_activity)

        // Trova i bottoni e imposta i listener
        findViewById<Button>(R.id.button_sit).setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.detect_fragment, StillFragment())
                commit()
            }
        }

        findViewById<Button>(R.id.button_walk).setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.detect_fragment, WalkFragment())
                commit()
            }
        }

        findViewById<Button>(R.id.button_vehicle).setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.detect_fragment, VehicleFragment())
                commit()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_ACTIVITY_RECOGNITION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContentView(R.layout.new_activity)
            }
        }
    }

    private fun askDetectPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            setContentView(R.layout.new_activity)
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            showPermissionDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE_ACTIVITY_RECOGNITION
            )
        }
    }

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
