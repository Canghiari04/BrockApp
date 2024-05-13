package com.example.ppt
package com.example.authenticator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.example.brockapp.R

class AuthenticatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authenticator_activity)

        findViewById<Button>(R.id.button_auth).setOnClickListener {
            authenticateBuiltIn()
        }
    }

    private fun authenticateBuiltIn() {
        val username = findViewById<EditText>(R.id.text_username).text.toString()
        val password = findViewById<EditText>(R.id.text_password).text.toString()

        // I CAN COMMUNICATE THE CREDENTIALS AND THEN RETURN A BOOLEAN VALUE THAT TELL ME IF THEY ARE TRUTHY OR FALSY

    }
}