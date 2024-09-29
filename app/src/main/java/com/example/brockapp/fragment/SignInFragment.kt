package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.MyNetwork
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.interfaces.NetworkAvailableImpl
import com.example.brockapp.permission.PostNotificationsPermission

import java.io.File
import android.util.Log
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider

class SignInFragment: Fragment(R.layout.fragment_sign_in) {
    private val networkUtil = NetworkAvailableImpl()
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var db: BrockDB
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var util: PostNotificationsPermission

    interface OnFragmentInteractionListener {
        fun showLoginFragment()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkConnectivity()

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]

        db = BrockDB.getInstance(requireContext())
        val file = File(context?.filesDir, "user_data.json")
        val s3Client = S3ClientProvider.getInstance(requireContext())

        val factoryUserViewModel = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        util = PostNotificationsPermission(requireActivity()) {
            setUpSharedPreferences()
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
                Toast.makeText(requireContext(), "Insert the credentials", Toast.LENGTH_LONG).show()
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
            Toast.makeText(requireContext(), "Check the connection to register yourself", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeNetwork() {
        viewModelNetwork.authNetwork.observe(viewLifecycleOwner) { authNetwork ->
            view?.findViewById<Button>(R.id.button_sign_in)?.isEnabled = authNetwork
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun observeSignIn() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                viewModelUser.getUser(username, password)
                util.requestPostNotificationPermission()
            } else {
                Toast.makeText(requireContext(), "Credentials already present", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUser() {
        viewModelUser.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                User.id = currentUser.id
                User.username = currentUser.username.toString()
                User.password = currentUser.password.toString()
            } else {
                Log.e("SIGN_IN_FRAGMENT", "User not found")
            }
        }
    }

    private fun setUpSharedPreferences() {
        // I create an unique shared preferences for every user signed in the app
        val sharedPreferences = requireContext().getSharedPreferences(
            "${User.id}_${User.username}_${User.password}",
            Context.MODE_PRIVATE
        )

        val editor = sharedPreferences?.edit()
        editor?.run {
            putBoolean("ACTIVITY_RECOGNITION", false)
            putBoolean("GEOFENCE_TRANSITION", false)
            putBoolean("DUMP_DATABASE", false)
        }
        editor?.apply()

        goToHome()
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "You")
        startActivity(intent)
        activity?.finish()
    }
}