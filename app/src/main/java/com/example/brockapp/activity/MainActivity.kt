package com.example.brockapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.brockapp.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<Button>(R.id.button_sign_in).setOnClickListener {
            callAuthenticator("Signin")
        }

        findViewById<Button>(R.id.button_login).setOnClickListener {
            callAuthenticator("Login")
        }

        findViewById<Button>(R.id.button_google).setOnClickListener {
            /* TODO --> CREDENTIAL MANAGER FOR THE GOOGLE LOGIN */
        }

        findViewById<Button>(R.id.button_web_view).setOnClickListener {
            /* TODO --> WEB VIEW TO SEE THE REPOSITORY OF THE PROJECT */
        }
    }

    private fun callAuthenticator(typeFragment : String) {
        startActivity(Intent(this, AuthenticatorActivity::class.java).putExtra("TYPE_PAGE", typeFragment))
    }
}