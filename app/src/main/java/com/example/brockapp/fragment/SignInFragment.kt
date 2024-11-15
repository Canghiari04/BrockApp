package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyNetwork
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.interfaces.NetworkAvailableImpl
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import java.io.File
import android.Manifest
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices

class SignInFragment: Fragment(R.layout.fragment_sign_in) {
    private val networkUtil = NetworkAvailableImpl()
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var db: BrockDB
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var util: PermissionUtil
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel

    interface OnFragmentInteractionListener {
        fun showLoginFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkConnectivity()

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]

        db = BrockDB.getInstance(requireContext())
        val file = File(context?.filesDir, "user_data.json")
        val s3Client = S3ClientProvider.getInstance(requireContext())

        val factoryUserViewModel = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        util = PermissionUtil(requireActivity()) {
            startBackgroundOperations()
        }

        observeNetwork()
        observeSignIn()
        observeUser()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.text_username).text.toString()
            password = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.registerUser(username, password)
            } else {
                Toast.makeText(requireContext(), "Inserisci le credenziali", Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.login_text_view).setOnClickListener {
            listener?.showLoginFragment()
        }
    }

    override fun onAttach(context: Context) {
        if(context is OnFragmentInteractionListener){
            listener = context
        }

        super.onAttach(context)
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun checkConnectivity() {
        if (networkUtil.isInternetActive(requireContext())) {
            MyNetwork.isConnected = true
        } else {
            MyNetwork.isConnected = false
            Toast.makeText(requireContext(), "Attiva la connessione per registrarti", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeNetwork() {
        viewModelNetwork.authNetwork.observe(viewLifecycleOwner) { authNetwork ->
            view?.findViewById<Button>(R.id.button_sign_in)?.isEnabled = authNetwork
        }
    }

    private fun observeSignIn() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                viewModelUser.getUser(username, password)
                util.requestPermissions()
            } else {
                Toast.makeText(requireContext(), "Credenziali di accesso già presenti", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUser() {
        viewModelUser.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                User.id = currentUser.id
                User.username = currentUser.username.toString()
                User.password = currentUser.password.toString()
                User.flag = currentUser.sharingFlag
            } else {
                Log.e("LOGIN_FRAGMENT", "User not found")
            }
        }
    }

    private fun startBackgroundOperations() {
        val factoryViewModelGeofence = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryViewModelGeofence)[GeofenceViewModel::class.java]

        observeGeofenceAreas()

        viewModelGeofence.fetchGeofenceAreas()
    }

    private fun observeGeofenceAreas() {
        viewModelGeofence.staticAreas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                MyGeofence.initAreas(areas)

                startGeofence()
            } else {
                viewModelGeofence.insertStaticGeofenceAreas()
            }
        }
    }

    private fun startGeofence() {
        val geofencingClient = LocationServices.getGeofencingClient(requireContext())

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(MyGeofence.request, MyGeofence.pendingIntent).run {
                addOnSuccessListener {
                    goToHome()
                }
                addOnFailureListener {
                    goToHome()
                    Log.e("GEOFENCING_RECEIVER", "Unsuccessful connection")
                }
            }
        } else {
            Log.wtf("GEOFENCE_PERMISSION", "Permission denied")
        }
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home")
        startActivity(intent)
        activity?.finish()
    }
}