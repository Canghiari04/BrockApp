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

class LoginFragment : Fragment(R.layout.fragment_login) {
    companion object {
        const val loginError: String = "CREDENZIALI ERRATE. SEI SICURO DI ESSERE GIÀ ISCRITTO ALL'APPLICAZIONE?"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val dbHelper = DbHelper(context)

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            val userAlreadyExists: Boolean = dbHelper.checkIfUserExists(username, password)
            if (userAlreadyExists) {

                val sharedPrefs = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("username", username)
                    apply()
                }

                startActivity(Intent(activity, PageLoaderActivity::class.java))
            } else {
                view.findViewById<TextView>(R.id.text_login_error).text = loginError
            }
        }
    }
}
