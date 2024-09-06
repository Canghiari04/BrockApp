package com.example.brockapp.activity


import com.example.brockapp.R
import com.example.brockapp.fragment.LoginFragment
import com.example.brockapp.fragment.SignInFragment

import android.os.Bundle
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity

class AuthenticatorActivity: AppCompatActivity(), LoginFragment.OnFragmentInteractionListener, SignInFragment.OnFragmentInteractionListener {
    private lateinit var loginFragment: LoginFragment
    private lateinit var signInFragment: SignInFragment
    private lateinit var receiver: AuthenticatorReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator)

        registerReceiver()

        supportActionBar?.hide()

        signInFragment = SignInFragment()
        loginFragment = LoginFragment()

        supportFragmentManager.beginTransaction().apply {
            add(R.id.auth_fragment, signInFragment)
            add(R.id.auth_fragment, loginFragment)
            hide(signInFragment)
            commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
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

    private fun registerReceiver() {
        receiver = AuthenticatorReceiver(this)

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
}