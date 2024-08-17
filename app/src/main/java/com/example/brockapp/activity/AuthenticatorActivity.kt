package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.fragment.LoginFragment
import com.example.brockapp.fragment.SignInFragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AuthenticatorActivity : AppCompatActivity(), LoginFragment.OnFragmentInteractionListener, SignInFragment.OnFragmentInteractionListener {
    private lateinit var signInFragment: SignInFragment
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authenticator_activity)

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