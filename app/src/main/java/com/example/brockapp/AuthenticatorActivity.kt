package com.example.brockapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.authenticator.LoginFragment
import com.example.brockapp.authenticator.SignInFragment

class AuthenticatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authenticator_activity)

        if(intent.getStringExtra("TYPE_PAGE").toString() == "SignIn") {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.auth_fragment, SignInFragment())
                commit()
            }
        } else {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.auth_fragment, LoginFragment())
                commit()
            }
        }
    }
}