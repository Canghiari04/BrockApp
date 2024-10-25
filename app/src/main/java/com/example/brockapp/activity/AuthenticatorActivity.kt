package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.fragment.LoginFragment
import com.example.brockapp.fragment.SignInFragment
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.interfaces.ShowAuthFragment
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.receiver.AuthenticatorReceiver
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.extraObject.MySharedPreferences

import java.io.File
import android.util.Log
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class AuthenticatorActivity: AppCompatActivity(), ShowAuthFragment {
    private lateinit var viewModel: UserViewModel
    private lateinit var loginFragment: LoginFragment
    private lateinit var signInFragment: SignInFragment
    private lateinit var broadCastReceiver: AuthenticatorReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator)

        supportActionBar?.hide()

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val factoryViewModel = UserViewModelFactory(db, s3Client, file)
        viewModel = ViewModelProvider(this, factoryViewModel)[UserViewModel::class.java]

        val (id, savedUsername, savedPassword) = MySharedPreferences.getCredentialsSaved(this)

        if (id != 0L && savedUsername != null && savedPassword != null) {
            observeUser()
            viewModel.getUser(savedUsername, savedPassword)
        } else {
            loginFragment = LoginFragment()
            signInFragment = SignInFragment()

            supportFragmentManager.beginTransaction().also {
                it.add(R.id.auth_fragment, loginFragment)
                it.add(R.id.auth_fragment, signInFragment)
                it.hide(signInFragment)
                it.commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver()
    }

    override fun showSignInFragment() {
        supportFragmentManager.beginTransaction().apply {
            hide(loginFragment)
            show(signInFragment)
            commit()
        }
    }

    override fun showLoginFragment() {
        supportFragmentManager.beginTransaction().apply {
            hide(signInFragment)
            show(loginFragment)
            commit()
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadCastReceiver)
    }

    private fun observeUser() {
        viewModel.currentUser.observe(this) { currentUser ->
            if (currentUser != null) {
                MyUser.also {
                    it.id = currentUser.id
                    it.username = currentUser.username
                    it.password = currentUser.password
                    it.typeActivity = currentUser.typeActivity
                    it.country = currentUser.country
                    it.city = currentUser.city
                }

                MySharedPreferences.setCredentialsSaved(this)
                goToHome()
            } else {
                Log.e("LOGIN_FRAGMENT", "User not found.")
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
        startActivity(intent)
        finish()
    }

    private fun registerReceiver() {
        broadCastReceiver = AuthenticatorReceiver(this)

        ContextCompat.registerReceiver(
            this,
            broadCastReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
}