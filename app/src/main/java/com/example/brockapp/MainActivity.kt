package com.example.brockapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // CLEAR SHARED PREFERENCES, ONLY FOR TEST AND DEVELOP OF THE PROJECT
        getSharedPreferences("AUTH_CREDENTIALS", Context.MODE_PRIVATE).edit().clear().apply()

        findViewById<Button>(R.id.button_sign_in).setOnClickListener {
            callAuthenticator("SignIn")
        }

        findViewById<Button>(R.id.button_login).setOnClickListener {
            callAuthenticator("Login")
        }

        findViewById<Button>(R.id.button_web_view).setOnClickListener {
            // TO DO THE WEB VIEW FOR THE REPOSITORY
        }
    }

    private fun callAuthenticator(typeFragment : String) {
        val authIntent : Intent = Intent(this, AuthenticatorActivity::class.java)
        authIntent.putExtra("TYPE_PAGE", typeFragment)
        startActivity(authIntent)
    }
}