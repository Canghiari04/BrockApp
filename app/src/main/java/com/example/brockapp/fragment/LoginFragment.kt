package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import java.io.File
import android.Manifest
import android.util.Log
import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.content.Context
import android.widget.TextView
import android.widget.EditText
import com.amazonaws.regions.Regions
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.google.android.gms.location.LocationServices
import com.amazonaws.auth.CognitoCachingCredentialsProvider

class LoginFragment: Fragment(R.layout.fragment_login) {
    private var user = User.getInstance()
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var util: PermissionUtil
    private lateinit var geofence: MyGeofence
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel

    /**
     * Uso di un'interfaccia per delegare l'implementazione del metodo desiderato dal fragment all'
     * activity owner.
     */
    interface OnFragmentInteractionListener {
        fun showSignInFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )
        val s3Client = AmazonS3Client(credentialsProvider)

        val file = File(requireContext().filesDir, "user_data.json")
        val db = BrockDB.getInstance(requireContext())

        val factoryViewModelUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        util = PermissionUtil(requireActivity()) {
            startBackgroundOperations()
        }

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.text_username).text.toString()
            password = view.findViewById<EditText>(R.id.text_password).text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.checkIfUserExistsLocally(username, password)
            } else {
                Toast.makeText(requireContext(), "Inserisci le credenziali", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<TextView>(R.id.signin_text_view).setOnClickListener {
            listener?.showSignInFragment()
        }

        observeLogin()
        observeUser()
    }

    override fun onAttach(context: Context) {
        if (context is OnFragmentInteractionListener)
            listener = context
        super.onAttach(context)
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun observeLogin() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                viewModelUser.getUser(username, password)
                util.requestPermissions()
            } else {
                Toast.makeText(requireContext(), "Credenziali errate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUser() {
        viewModelUser.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                user.id = currentUser.id
                user.username = currentUser.username.toString()
                user.password = currentUser.password.toString()
                user.flag = currentUser.sharingFlag
            } else {
                Log.d("LOGIN_FRAGMENT", "User not found.")
            }
        }
    }

    private fun startBackgroundOperations() {
        val db = BrockDB.getInstance(requireContext())
        val factoryViewModelGeofence = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryViewModelGeofence)[GeofenceViewModel::class.java]

        observeGeofenceAreas()
    }

    private fun observeGeofenceAreas() {
        viewModelGeofence.staticAreas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                geofence = MyGeofence.getInstance()
                geofence.initAreas(areas)

                startGeofence()
            } else {
                viewModelGeofence.insertStaticGeofenceAreas()
            }
        }
    }

    private fun startGeofence() {
        val geofencingClient = LocationServices.getGeofencingClient(requireContext())

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofence.pendingIntent.let {
                geofencingClient.addGeofences(geofence.request, it).run {
                    addOnSuccessListener {
                        goToHome()
                    }
                    addOnFailureListener {
                        Log.e("GEOFENCING_RECEIVER", "Unsuccessful connection.")
                        goToHome()
                    }
                }
            }
        } else {
            Log.e("GEOFENCE_PERMISSION", "Missing permission.")
        }
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home")
        startActivity(intent)
        activity?.finish()
    }
}