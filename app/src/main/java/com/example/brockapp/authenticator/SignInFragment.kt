package com.example.brockapp.authenticator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.brockapp.PageLoaderActivity
import com.example.brockapp.R
import com.example.brockapp.database.DbHelper

class SignInFragment : Fragment(R.layout.fragment_sign_in) {
    companion object {
        const val successSignIn : String = "CREAZIONE ACCOUNT AVVENUTA"
        const val blankError : String = "CREDENZIALI ERRATE. NON HAI INSERITO TUTTI I CAMPI NEL FORM"
        const val signInCredentialsError: String = "CREDENZIALI GIA' PRESENTI. UTILIZZA UN ALTRO USERNAME E PASSWORD"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var obj = BuiltInAuthenticator()
        val context = requireContext()
        val dbHelper = DbHelper(context)

        getView()?.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {

            var username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            var password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isBlank() || password.isBlank()) {
                view.findViewById<TextView>(R.id.text_sign_in_error).text = blankError
            }
            else {
                if(obj.authCredentials(username, password, activity?.getSharedPreferences("AUTH_CREDENTIALS", Context.MODE_PRIVATE))) {
                    view.findViewById<TextView>(R.id.text_sign_in_error).text = signInCredentialsError
                }
                else {
                    obj.addCredentials(username, password, activity?.getSharedPreferences("AUTH_CREDENTIALS", Context.MODE_PRIVATE))
                    view.findViewById<TextView>(R.id.text_sign_in_error).text = successSignIn

                    dbHelper.insertUser(dbHelper, username, password)

                    val sharedPrefs = activity?.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)

                    if (sharedPrefs != null) {
                        with (sharedPrefs.edit()) {
                            putString("username", username)
                            apply()
                        }
                    }

                    startActivity(Intent(activity, PageLoaderActivity::class.java))
                }
            }
        }
    }
}