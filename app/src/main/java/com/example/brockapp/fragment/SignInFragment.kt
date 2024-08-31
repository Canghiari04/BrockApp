package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import java.io.File
import android.Manifest
import android.util.Log
import android.os.Bundle
import android.view.View
import org.json.JSONObject
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.content.Context
import android.widget.TextView
import kotlinx.coroutines.launch
import android.content.IntentFilter
import com.amazonaws.regions.Regions
import kotlinx.coroutines.Dispatchers
import androidx.fragment.app.Fragment
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.google.android.gms.location.LocationServices
import com.amazonaws.auth.CognitoCachingCredentialsProvider

class SignInFragment : Fragment(R.layout.fragment_sign_in) {
    private var user = User.getInstance()
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var s3Client: AmazonS3Client

    private lateinit var db : BrockDB
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var util: PermissionUtil
    private lateinit var geofence: MyGeofence
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGeofence: GeofenceViewModel

    /**
     * Uso di un'interfaccia per delegare l'implementazione del metodo desiderato dal fragment all'
     * activity owner.
     */
    interface OnFragmentInteractionListener {
        fun showLoginFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = BrockDB.getInstance(requireContext())

        val credentialsProvider = CognitoCachingCredentialsProvider(
            requireContext(),
            "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
            Regions.EU_WEST_3
        )
        s3Client = AmazonS3Client(credentialsProvider)

        val factoryUserViewModel = UserViewModelFactory(db)
        viewModelUser = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        util = PermissionUtil(requireActivity()) {
            startBackgroundOperations()
        }

        observeSignIn()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.text_username).text.toString()
            password = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.registerUser(username, password)
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.login_text_view).setOnClickListener {
            listener?.showLoginFragment()
        }
    }

    override fun onAttach(context: Context) {
        if(context is OnFragmentInteractionListener)
            listener = context
        super.onAttach(context)
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun observeSignIn() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                util.requestPermissions()

                user.username = username
                user.password = password
                user.flag = false

                CoroutineScope(Dispatchers.IO).launch {
                    user.id = db.UserDao().getIdFromUsernameAndPassword(username, password)
                }
            } else {
                Toast.makeText(requireContext(), SIGN_IN_ERROR, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startBackgroundOperations() {
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
                        startConnectivity()
                        goToHome()
                    }
                    addOnFailureListener {
                        Log.e("GEOFENCING_RECEIVER", "Unsuccessful connection.")
                    }
                }
            }
        } else {
            Log.e("GEOFENCE_PERMISSION", "Missing permission.")
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

    // POSTICIPARE IL BUCKET SOLO NEL MOMENTO IN CUI SIA DATO IL PERMESSO IN ONCLICK FRIENDS
    private fun uploadUserDataToS3(username: String) {
        val userData = mapOf("username" to username)
        val json = JSONObject(userData).toString()

        val fileName = "user_data.json"
        val file = File(requireContext().filesDir, fileName)
        file.writeText(json)

        val key = "user/$username.json"

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