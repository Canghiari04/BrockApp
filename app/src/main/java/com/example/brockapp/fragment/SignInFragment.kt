package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.`object`.SharedPreferences
import com.example.brockapp.`object`.MyUser
import com.example.brockapp.database.BrockDB
import com.example.brockapp.`object`.MyNetwork
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.interfaces.NetworkAvailableImpl
import com.example.brockapp.util.PostNotificationsPermissionUtil

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
    private lateinit var util: PostNotificationsPermissionUtil

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
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val factoryUserViewModel = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        util = PostNotificationsPermissionUtil(requireActivity()) {
            observeUser()
        }

        observeNetwork()
        observeSignIn()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.text_username).text.toString()
            password = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.registerUser(username, password)
            } else {
                Toast.makeText(requireContext(), "Insert the access credentials", Toast.LENGTH_LONG).show()
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
            Toast.makeText(requireContext(), "Check the connection to register your account", Toast.LENGTH_LONG).show()
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
                MyUser.id = currentUser.id
                MyUser.username = currentUser.username!!
                MyUser.password = currentUser.password!!

                SharedPreferences.setUpSharedPreferences(requireContext())
                goToHome()
            } else {
                Log.e("SIGN_IN_FRAGMENT", "User not found")
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "You")
        startActivity(intent)
        activity?.finish()
    }
}