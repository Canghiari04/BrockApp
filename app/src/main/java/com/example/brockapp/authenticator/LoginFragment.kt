package com.example.brockapp.authenticator

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.LOGIN_ERROR
import com.example.brockapp.database.BrockDB
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.activity.AuthenticatorActivity

import android.util.Log
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import android.content.pm.PackageManager

import androidx.fragment.app.Fragment
import com.example.brockapp.activity.AuthenticatorActivity
import androidx.core.app.ActivityCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.example.brockapp.R
import androidx.lifecycle.lifecycleScope

class LoginFragment: Fragment(R.layout.login_fragment) {
    private val listPermissions = ArrayList<String>()

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val userDao = db.UserDao()

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val userAlreadyExists = withContext(Dispatchers.IO) {
                        userDao.checkIfUserIsPresent(username, password)
                    }

                    // Condizione definita per accertarsi che l'utente sia iscritto all'applicazione.
                    if(userAlreadyExists) {
                        // Istanza del singoletto, utilizzata per memorizzare le informazione di accesso dell'utente.
                        val user = User.getInstance()

                        user.id = withContext(Dispatchers.IO) {
                            userDao.getIdFromUsernameAndPassword(username, password)
                        }
                        user.username = username
                        user.password = password

                        startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
                    } else {
                        Toast.makeText(requireContext(), LOGIN_ERROR, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }
                if (hasPermissions(requireContext(), PERMISSIONS)) {
                    startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
                } else {
                    if(!shouldShowRationaleDialog(PERMISSIONS)) {
                        permissionLauncher.launch(PERMISSIONS)
                    } else {
                        showLocationPermissionRationaleDialog(requireContext())
                    }
                }
            } else {
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

    /**
     * Metodo attuato per mostrare la finesta di dialogo successiva a differenti eventi "Deny"
     * dei permessi richiesti. Il metodo risveglierà l'activity settings del dispositivo.
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
}