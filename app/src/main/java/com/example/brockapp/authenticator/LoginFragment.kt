package com.example.brockapp.authenticator

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.LOGIN_ERROR
import com.example.brockapp.database.BrockDB
import com.example.brockapp.PERMISSIONS_LOCATION
import com.example.brockapp.PERMISSION_BACKGROUND
import com.example.brockapp.activity.MainActivity
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.activity.AuthenticatorActivity

import android.net.Uri
import android.util.Log
import android.Manifest
import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import kotlinx.coroutines.launch
import android.provider.Settings
import kotlinx.coroutines.withContext
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts

class LoginFragment: Fragment(R.layout.login_fragment) {
    private val listPermissions = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val db = BrockDB.getInstance(requireContext())
            val userDao = db.UserDao()

            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val userAlreadyExists = withContext(Dispatchers.IO) {
                        userDao.checkIfUserIsPresent(username, password)
                    }

                    if (userAlreadyExists) {
                        val user = User.getInstance()

                        user.id = withContext(Dispatchers.IO) {
                            userDao.getIdFromUsernameAndPassword(username, password)
                        }
                        user.username = username
                        user.password = password


                        checkLocationPermissions()
                    } else {
                        Toast.makeText(requireContext(), LOGIN_ERROR, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.signin_text_view).setOnClickListener {
            startActivity(Intent(activity, AuthenticatorActivity::class.java).putExtra("TYPE_PAGE", "Signin"))
        }
    }

    private fun checkLocationPermissions() {
        when {
            hasLocationPermissions(requireContext(), PERMISSIONS_LOCATION) -> {
                checkBackgroundPermission()
            }
            shouldShowLocationPermissionsRationaleDialog(PERMISSIONS_LOCATION) -> {
                showPermissionsRationaleDialog(requireContext())
            }
            else -> {
                permissionsLocationLauncher.launch(PERMISSIONS_LOCATION)
            }
        }
    }

    private fun hasLocationPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowLocationPermissionsRationaleDialog(permissions: Array<String>): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)
        }
    }

    private fun checkBackgroundPermission() {
        when {
            hasBackgroundPermission(requireContext()) -> {
                startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
            }
            shouldShowBackgroundPermissionRationaleDialog() -> {
                showPermissionsRationaleDialog(requireContext())
            }
            else -> {
                permissionBackGroundLauncher.launch(PERMISSION_BACKGROUND)
            }
        }
    }

    private fun hasBackgroundPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, PERMISSION_BACKGROUND) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowBackgroundPermissionRationaleDialog(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), PERMISSION_BACKGROUND)
    }

    /**
     * Metodo attuato per mostrare la finestra di dialogo successiva al "Deny" dei permessi
     * richiesti.
     */
    private fun showPermissionsRationaleDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }

    /**
     * Variabile privata di tipo ActivityResultLauncher, utilizzata per accertarsi se l'utente abbia
     * accettato o meno i permessi richiesti.
     */
    private val permissionsLocationLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
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

        if(listPermissions.isNotEmpty()) {
            showLocationPermissionsDialog(requireContext())
        } else {
            permissionBackGroundLauncher.launch(PERMISSION_BACKGROUND)
        }
    }

    /**
     * Metodo attuato per mostrare la finestra di dialogo successiva al "Deny" dei permessi
     * richiesti.
     */
    private fun showLocationPermissionsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                checkLocationPermissions()
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }

    private val permissionBackGroundLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
        } else {
            showBackgroundPermissionDialog(requireContext())
        }
    }

    private fun showBackgroundPermissionDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions_title)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                checkBackgroundPermission()
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }
}