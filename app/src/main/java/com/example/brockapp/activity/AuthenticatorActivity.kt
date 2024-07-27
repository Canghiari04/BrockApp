package com.example.brockapp.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.R
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
        } else if(intent.getStringExtra("TYPE_PAGE").toString() == "LogIn") {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.auth_fragment, LoginFragment())
                commit()
            }
        } else {
            Log.d("WTF", "WTF")
        }
    }
}