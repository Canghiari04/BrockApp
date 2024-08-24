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
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.BUCKET_NAME
import com.example.brockapp.R
import com.example.brockapp.SIGN_IN_ERROR
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
import org.json.JSONObject
import java.io.File

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var util: PermissionUtil
    private lateinit var geofence: MyGeofence
    private lateinit var viewModel: UserViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel
    private lateinit var credentialsProvider: CognitoCachingCredentialsProvider

    private lateinit var s3Client: AmazonS3Client

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

        credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )

        s3Client = AmazonS3Client(credentialsProvider)

        util = PermissionUtil(requireActivity()) {
            startBackgroundOperations()
        }

        viewModel = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        observeSignIn()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {


            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {

                viewModel.registerUser(username, password)
                uploadUserDataToS3(username, password)

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
                util.requestPermissions()
            } else {
                Toast.makeText(requireContext(), SIGN_IN_ERROR, Toast.LENGTH_SHORT).show()
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
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Home")
        startActivity(intent)
        activity?.finish()
    }


    private fun uploadUserDataToS3(username: String, password: String) {
        val userData = mapOf("username" to username, "password" to password)
        val json = JSONObject(userData).toString()

        // Create a file in the app's private storage directory
        val fileName = "user_data.json"
        val file = File(requireContext().filesDir, fileName)
        file.writeText(json)

        // Define the key (path) under which the file will be stored in the bucket
        val key = "user/$username.json"

        // Start a background thread for the upload to avoid blocking the UI
        val thread = Thread {
            try {
                val request = PutObjectRequest(BUCKET_NAME, key, file)
                s3Client.putObject(request)
                Log.d("S3Upload", "User data uploaded successfully")
            } catch (e: Exception) {
                Log.e("S3Upload", "Failed to upload user data", e)
            }
        }
        thread.start()
    }

}