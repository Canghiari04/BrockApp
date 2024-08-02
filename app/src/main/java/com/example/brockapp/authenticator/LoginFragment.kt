package com.example.brockapp.authenticator

import com.example.brockapp.User
import com.example.brockapp.database.DbHelper
import com.example.brockapp.activity.MainActivity
import com.example.brockapp.activity.PageLoaderActivity

import android.util.Log
import android.Manifest
import android.view.View
import android.os.Bundle
import android.widget.Button
import com.example.brockapp.R
import android.content.Intent
import android.widget.EditText
import android.content.Context
import android.app.AlertDialog
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts

class LoginFragment : Fragment(R.layout.login_fragment) {
    private val listPermissions = ArrayList<String>()

    companion object {
        const val LOGINERROR: String = "CREDENZIALI ERRATE. SEI SICURO DI ESSERE GIÀ ISCRITTO ALL'APPLICAZIONE?"
        val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User.getInstance()
        val dbHelper = DbHelper(requireContext())

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            val userId: Long = dbHelper.getUserId(username, password)

            if (userId != -1L) {
                user.id = userId
                user.username = username
                user.password = password

                if (hasPermissions(requireContext(), PERMISSIONS)) {
                    startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
                } else {
                    if (shouldShowRequestPermissionRationale()) {
                        showPermissionRationaleDialog()
                    } else {
                        permissionsLauncher.launch(PERMISSIONS)
                    }
                }
            } else {
                view.findViewById<TextView>(R.id.text_login_error).text = LOGINERROR
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        return PERMISSIONS.any { ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it) }
    }

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        listPermissions.clear()

        for (permission in permissions) {
            when (permission.key) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    if (!permission.value) listPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (!permission.value) listPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                else -> Log.d("WTF", "WTF")
            }
        }

        if (listPermissions.isEmpty()) {
            startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
        } else {
            showPermissionDialog(requireContext())
        }
    }

    private fun showPermissionDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                permissionsLauncher.launch(PERMISSIONS)
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("CIAO")
            .setMessage("CIAO")
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                // TODO PORTARLO AI SETTINGS
                dialog.dismiss()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            .create()
            .show()
    }
}