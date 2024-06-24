package com.example.brockapp.authenticator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.brockapp.HomeActivity
import com.example.brockapp.R

class LoginFragment : Fragment(R.layout.fragment_login) {
    companion object {
        const val blankError : String = "CREDENZIALI ERRATE. NON HAI INSERITO TUTTI I CAMPI NEL FORM"
        const val loginError : String = "CREDENZIALI ERRATE. SEI SICURO DI ESSERE GIÀ ISCRITTO ALL'APPLICAZIONE?"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val obj = BuiltInAuthenticator()

        getView()?.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val username : String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password : String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isBlank() || password.isBlank()) { // CHECK IF THE FIELDS ARE BLANK
                view.findViewById<TextView>(R.id.text_login_error).text = blankError
            } else {
                if(obj.authCredentials(username, password, activity?.getSharedPreferences("AUTH_CREDENTIALS", Context.MODE_PRIVATE)))
                    startActivity(Intent(activity, HomeActivity::class.java)) // START THE HOMEPAGE ACTIVITY
                else {
                    view.findViewById<TextView>(R.id.text_login_error).text = loginError
                }
            }
        }
    }
}