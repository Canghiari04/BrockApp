package com.example.ppt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.brockapp.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<Button>(R.id.button_access).setOnClickListener {
            callAuthenticator()
        }
    }

    private fun callAuthenticator() {
        Intent(this, AuthenticatorActivity::class.java).also {
            startActivity(it)
        }
    }
}