package com.example.brockapp.authenticator

import com.example.brockapp.data.User
import com.example.brockapp.database.DbHelper
import com.example.brockapp.activity.PageLoaderActivity

import android.view.View
import android.os.Bundle
import android.widget.Button
import com.example.brockapp.R
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class LoginFragment : Fragment(R.layout.login_fragment) {
    companion object {
        const val loginError: String = "CREDENZIALI ERRATE. SEI SICURO DI ESSERE GIÀ ISCRITTO ALL'APPLICAZIONE?"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Istanza del singoletto, utilizzata per memorizzare le informazione di accesso dell'utente.
        val user = User.getInstance()
        val dbHelper = DbHelper(requireContext())

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            val userId: Long = dbHelper.getUserId(username, password)

            // Condizione definita per accertarsi che l'utente sia iscritto all'applicazione.
            if (userId != -1L) {
                user.id = userId
                user.username = username
                user.password = password

                startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
            } else {
                view.findViewById<TextView>(R.id.text_login_error).text = loginError
            }
        }
    }
}
