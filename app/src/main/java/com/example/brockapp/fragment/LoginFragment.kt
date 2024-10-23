package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.activity.AuthenticatorActivity
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.util.PostNotificationsPermissionUtil

import java.io.File
import android.util.Log
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider

class LoginFragment: Fragment(R.layout.fragment_login) {
    private val toastUtil = ShowCustomToastImpl()

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var viewModelUser: UserViewModel
    private lateinit var util: PostNotificationsPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val file = File(requireContext().filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val factoryViewModelUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        val (id, savedUsername, savedPassword) = MySharedPreferences.getCredentialsSaved(requireContext())

        // If the user is already sign in he can pass to the page loader activity
        if (id != 0L && savedUsername != null && savedPassword != null) {
            view.findViewById<View>(R.id.view_login).visibility = View.GONE

            observeUser()

            viewModelUser.getUser(savedUsername, savedPassword)
        } else {
            util = PostNotificationsPermissionUtil(requireActivity()) {
                observeUser()
            }

            observeLogin()

            view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
                username = view.findViewById<EditText>(R.id.edit_text_username).text.toString()
                password = view.findViewById<EditText>(R.id.edit_text_password).text.toString()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    viewModelUser.checkIfUserExistsLocally(username, password)
                } else {
                    toastUtil.showWarningToast(
                        "You must insert the field required",
                        requireContext()
                    )
                }
            }
        }

        view.findViewById<TextView>(R.id.text_view_sign_in).setOnClickListener {
            (requireActivity() as AuthenticatorActivity).showSignInFragment()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun observeLogin() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                viewModelUser.getUser(username, password)
                util.requestPostNotificationPermission()
            } else {
                toastUtil.showWarningToast(
                    "Access credentials are wrong",
                    requireContext()
                )
            }
        }
    }

    private fun observeUser() {
        viewModelUser.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                MyUser.also {
                    it.id = currentUser.id
                    it.username = currentUser.username
                    it.password = currentUser.password
                    it.typeActivity = currentUser.typeActivity
                    it.country = currentUser.country
                    it.city = currentUser.city
                }

                MySharedPreferences.setCredentialsSaved(requireContext())
                goToHome()
            } else {
                Log.e("LOGIN_FRAGMENT", "User not found.")
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
        startActivity(intent)
        activity?.finish()
    }
}