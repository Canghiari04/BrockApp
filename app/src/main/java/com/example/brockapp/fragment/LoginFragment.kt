package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.PermissionUtil
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.viewmodel.UserViewModelFactory

import java.io.File
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class LoginFragment: Fragment(R.layout.fragment_login) {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var util: PermissionUtil
    private lateinit var viewModelUser: UserViewModel

    interface OnFragmentInteractionListener {
        fun showSignInFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val file = File(requireContext().filesDir, "user_data.json")
        val s3Client = S3ClientProvider.getInstance(requireContext())

        val factoryViewModelUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        util = PermissionUtil(requireActivity()) {
            goToHome()
        }

        observeLogin()
        observeUser()

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.text_username).text.toString()
            password = view.findViewById<EditText>(R.id.text_password).text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.checkIfUserExistsLocally(username, password)
            } else {
                Toast.makeText(requireContext(), "Inserisci le credenziali di accesso", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<TextView>(R.id.signin_text_view).setOnClickListener {
            listener?.showSignInFragment()
        }
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
                Toast.makeText(requireContext(), "Credenziali di accesso errate", Toast.LENGTH_SHORT).show()
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
                Log.e("LOGIN_FRAGMENT", "User not found.")
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}