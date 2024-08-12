package com.example.brockapp.authenticator

import com.example.brockapp.R
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.SIGN_IN_ERROR
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.activity.MainActivity
import com.example.brockapp.manager.GeofenceManager
import com.example.brockapp.service.GeofenceService
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.activity.AuthenticatorActivity
import com.example.brockapp.viewmodel.UserViewModelFactory

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.GeofencingClient
import androidx.activity.result.contract.ActivityResultContracts

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    private val listPermissions = mutableListOf<String>()

    private lateinit var viewModelUser: UserViewModel
    private lateinit var utilPermission: PermissionUtil
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var geofencingClient: GeofencingClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                val db = BrockDB.getInstance(requireContext())
                val factoryViewModelUser = UserViewModelFactory(db)
                viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

                viewModelUser.authSignIn(username, password)

                viewModelUser.auth.observe(viewLifecycleOwner) { item ->
                    if(item) {
                        utilPermission = PermissionUtil(requireContext(), requireActivity())
                        geofenceManager = GeofenceManager(requireContext())

                        checkLocationPermissions()
                    } else {
                        Toast.makeText(requireContext(), SIGN_IN_ERROR, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.login_text_view).setOnClickListener {
            startActivity(Intent(activity, AuthenticatorActivity::class.java).putExtra("TYPE_PAGE", "Login"))
        }
    }

    private fun checkLocationPermissions() {
        when {
            utilPermission.hasLocationPermissions(requireContext(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) -> {
                checkBackgroundPermission()
            }
            utilPermission.shouldShowLocationPermissionsRationaleDialog(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) -> {
                utilPermission.showPermissionsRationaleDialog()
            }
            else -> {
                permissionsLocationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    private fun checkBackgroundPermission() {
        when {
            utilPermission.hasBackgroundPermission(requireContext()) -> {
                checkNotificationPermission()
            }
            utilPermission.shouldShowBackgroundPermissionRationaleDialog() -> {
                utilPermission.showPermissionsRationaleDialog()
            }
            else -> {
                permissionBackGroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun checkNotificationPermission() {
        when {
            utilPermission.hasNotificationPermission(requireContext()) -> {
                startGeofenceBroadcast()
                goToHome()
            }
            utilPermission.shouldShowNotificationPermissionRationaleDialog() -> {
                utilPermission.showPermissionsRationaleDialog()
            }
            else -> {
                permissionNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
            }
        }

        if(listPermissions.isNotEmpty()) {
            showLocationPermissionsDialog(requireContext())
        } else {
            permissionBackGroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private val permissionBackGroundLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            permissionNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showBackgroundPermissionDialog(requireContext())
        }
    }

    private val permissionNotificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            startGeofenceBroadcast()
            goToHome()
        } else {
            showNotificationPermissionDialog(requireContext())
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

    private fun showBackgroundPermissionDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_background)
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

    private fun showNotificationPermissionDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_notification)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                checkNotificationPermission()
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(context, MainActivity::class.java))
            }
            .create()
            .show()
    }

    private fun goToHome() {
        startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
    }

    /**
     * Connesso alla REMOTE API è "risvegliato" il service contenente il broadcast receiver per
     * gestire eventi di geofencing.
     */
    private fun startGeofenceBroadcast() {
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofenceManager.getRequest(), geofenceManager.getPendingIntent()).run {
                addOnSuccessListener {
                    activity?.startService(Intent(activity, GeofenceService::class.java))
                }
                addOnFailureListener {
                    // TODO -> GESTIONE QUALORA NON SIA ABBIA CONNESSIONE AD INTERNET
                }
            }
        }
    }
}