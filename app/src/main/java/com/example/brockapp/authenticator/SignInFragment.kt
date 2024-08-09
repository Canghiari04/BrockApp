package com.example.brockapp.authenticator

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.database.DbHelper
import com.example.brockapp.activity.MainActivity
import com.example.brockapp.activity.PageLoaderActivity

import android.util.Log
import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    private val listPermissions = ArrayList<String>()

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User.getInstance()
        val dbHelper = DbHelper(requireContext())

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            val userAlreadyExists: Boolean = dbHelper.checkIfUserIsPresent(username, password)

            if(userAlreadyExists){
            } else {
                val userId: Long = dbHelper.insertUser(dbHelper, username, password)

                user.id = userId
                user.username = username
                user.password = password

                if(hasPermissions(requireContext(), PERMISSIONS)) {
                    startActivity(Intent(activity, PageLoaderActivity::class.java))
                } else {
                    if(shouldShowRationaleDialog(PERMISSIONS)) {
                        showLocationPermissionRationaleDialog(requireContext())
                    } else {
                        permissionLauncher.launch(PERMISSIONS)
                    }
                }
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRationaleDialog(permissions: Array<String>): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)
        }
    }

    /**
     * Variabile privata di tipo ActivityResultLauncher, utilizzata per accertarsi se l'utente abbia
     * accettato o meno i permessi richiesti.
     */
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        listPermissions.clear()

        for (permission in permissions) {
            when (permission.key) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    if (!permission.value)
                        listPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (!permission.value)
                        listPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                else -> Log.d("WTF", "WTF")
            }
        }

        if (listPermissions.isEmpty()) {
            startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
        } else {
            showLocationPermissionDialog(requireContext())
        }
    }

    /**
     * Metodo attuato per mostrare la finestra di dialogo successiva al "Deny" dei permessi
     * richiesti.
     */
    private fun showLocationPermissionRationaleDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }

    /**
     * Metodo attuato per mostrare la finesta di dialogo successiva a differenti eventi "Deny"
     * dei permessi richiesti. Il metodo risveglierà l'activity settings del dispositivo.
     */
    private fun showLocationPermissionDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                permissionLauncher.launch(PERMISSIONS)
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }
}