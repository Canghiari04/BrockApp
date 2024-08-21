package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.activity.PageLoaderActivity
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
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    private val listPermissions = mutableListOf<String>()
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var util: PermissionUtil
    private lateinit var viewModel: UserViewModel

    /**
     * Uso di un'interfaccia per delegare l'implementazione del metodo desiderato dal fragment all'
     * activity owner.
     */
    interface OnFragmentInteractionListener {
        fun showLoginFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val factoryUserViewModel = UserViewModelFactory(db)

        util = PermissionUtil(requireContext(), requireActivity())
        viewModel = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        observeSignIn()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.registerUser(username, password)
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.login_text_view).setOnClickListener {
            listener?.showLoginFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnFragmentInteractionListener)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun observeSignIn() {
        viewModel.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                checkLocationPermissions()
            } else {
                Toast.makeText(requireContext(), SIGN_IN_ERROR, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermissions() {
        when {
            util.hasLocationPermissions(requireContext(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) -> {
                checkBackgroundPermission()
            }
            util.shouldShowLocationPermissionsRationaleDialog(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) -> {
                util.showPermissionsRationaleDialog()
            }
            else -> {
                permissionsLocationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    private fun checkBackgroundPermission() {
        when {
            util.hasBackgroundPermission(requireContext()) -> {
                checkNotificationPermission()
            }
            util.shouldShowBackgroundPermissionRationaleDialog() -> {
                util.showPermissionsRationaleDialog()
            }
            else -> {
                permissionBackGroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun checkNotificationPermission() {
        when {
            util.hasNotificationPermission(requireContext()) -> {
                goToHome()
            }
            util.shouldShowNotificationPermissionRationaleDialog() -> {
                util.showPermissionsRationaleDialog()
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
            .setMessage(R.string.permissions_location)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                checkLocationPermissions()
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
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
            }
            .create()
            .show()
    }

    private fun goToHome() {
        startActivity(Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home"))
    }
}