package com.example.brockapp.authenticator

import com.example.brockapp.User
import com.example.brockapp.database.DbHelper
import com.example.brockapp.activity.PageLoaderActivity

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.brockapp.R
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    companion object {
        const val signInCredentialsError: String = "CREDENZIALI GIA' PRESENTI. UTILIZZA UN ALTRO USERNAME E PASSWORD"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User.getInstance()
        val dbHelper = DbHelper(requireContext())

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            val userAlreadyExists: Boolean = dbHelper.checkIfUserIsPresent(username, password)

            if(userAlreadyExists){
                view.findViewById<TextView>(R.id.text_sign_in_error).text = signInCredentialsError
            } else {
                val userId: Long = dbHelper.insertUser(dbHelper, username, password)

                user.id = userId
                user.username = username
                user.password = password

                startActivity(Intent(activity, PageLoaderActivity::class.java))
            }
        }
    }
}