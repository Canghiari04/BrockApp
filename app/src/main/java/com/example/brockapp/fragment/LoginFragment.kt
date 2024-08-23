package com.example.brockapp.fragment

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.LOGIN_ERROR
import com.example.brockapp.R
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.database.BrockDB
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.singleton.User
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.GeofenceViewModelFactory
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.login_fragment) {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var util: PermissionUtil
    private lateinit var geofence: MyGeofence
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel

    private lateinit var db : BrockDB

    private lateinit var username : String
    private lateinit var password : String


    /**
     * Uso di un'interfaccia per delegare l'implementazione del metodo desiderato dal fragment all'
     * activity owner.
     */
    interface OnFragmentInteractionListener {
        fun showSignInFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = BrockDB.getInstance(requireContext())
        val factoryViewModelUser = UserViewModelFactory(db)

        util = PermissionUtil(requireActivity()) {
            startBackgroundOperations()
        }

        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        observeLogin()

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.text_username).text.toString()
            password= view.findViewById<EditText>(R.id.text_password).text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.checkIfUserExists(username, password)
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.signin_text_view).setOnClickListener {
            listener?.showSignInFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun observeLogin() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                util.requestPermissions()
                CoroutineScope(Dispatchers.IO).launch {
                    User.id = db.UserDao().getIdFromUsernameAndPassword(username, password)
                }

            } else {
                Toast.makeText(requireContext(), LOGIN_ERROR, Toast.LENGTH_LONG).show()
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
        viewModelGeofence.areas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                geofence = MyGeofence.getInstance()
                geofence.initGeofences(areas)

                startGeofence()
            } else {
                viewModelGeofence.insertStaticGeofenceAreas()
            }
        }
    }

    private fun startGeofence() {
        val geofencingClient = LocationServices.getGeofencingClient(requireContext())

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofence.pendingIntent?.let {
                geofencingClient.addGeofences(geofence.request, it).run {
                    addOnSuccessListener {
                        startConnectivity()
                        goToHome()
                    }
                    addOnFailureListener {
                        Log.e("GEOFENCING_RECEIVER", "Unsuccessful connection.")
                    }
                }
            }
        } else {
            Log.d("WTF", "WTF")
        }
    }

    private fun startConnectivity() {
        val receiver = ConnectivityReceiver()

        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun goToHome() {
        startActivity(Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home"))
    }
}