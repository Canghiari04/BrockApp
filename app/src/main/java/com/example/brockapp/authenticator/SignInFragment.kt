package com.example.brockapp.authenticator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.R
import com.example.brockapp.database.DbHelper

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    companion object {
        const val signInCredentialsError: String = "CREDENZIALI GIA' PRESENTI. UTILIZZA UN ALTRO USERNAME E PASSWORD"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val dbHelper = DbHelper(context)

        getView()?.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {

            var username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            var password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            var userAlreadyExists : Boolean = dbHelper.checkIfUserExists(username, password)

            if(userAlreadyExists){
                view.findViewById<TextView>(R.id.text_sign_in_error).text = signInCredentialsError
            }
            else {
                dbHelper.insertUser(dbHelper, username, password)

                val sharedPrefs = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("username", username)
                    apply()
                }
                startActivity(Intent(activity, PageLoaderActivity::class.java))
            }
        }
    }
}