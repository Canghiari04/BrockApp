package com.example.brockapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.R
import com.example.brockapp.fragment.LoginFragment
import com.example.brockapp.fragment.SignInFragment

class AuthenticatorActivity: AppCompatActivity(), LoginFragment.OnFragmentInteractionListener, SignInFragment.OnFragmentInteractionListener {
    private lateinit var loginFragment: LoginFragment
    private lateinit var signInFragment: SignInFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator)

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
}