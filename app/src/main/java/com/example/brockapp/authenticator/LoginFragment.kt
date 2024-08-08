package com.example.brockapp.authenticator

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.LOGIN_ERROR
import com.example.brockapp.database.BrockDB
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.activity.AuthenticatorActivity

import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

class LoginFragment : Fragment(R.layout.login_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val userDao = db.UserDao()

        view.findViewById<Button>(R.id.button_login)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val userAlreadyExists = withContext(Dispatchers.IO) {
                        userDao.checkIfUserIsPresent(username, password)
                    }

                    // Condizione definita per accertarsi che l'utente sia iscritto all'applicazione.
                    if(userAlreadyExists) {
                        // Istanza del singoletto, utilizzata per memorizzare le informazione di accesso dell'utente.
                        val user = User.getInstance()

                        user.id = withContext(Dispatchers.IO) {
                            userDao.getIdFromUsernameAndPassword(username, password)
                        }
                        user.username = username
                        user.password = password

                        startActivity(Intent(requireContext(), PageLoaderActivity::class.java))
                    } else {
                        Toast.makeText(requireContext(), LOGIN_ERROR, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.signin_text_view).setOnClickListener {
            startActivity(Intent(activity, AuthenticatorActivity::class.java).putExtra("TYPE_PAGE", "SignIn"))
        }
    }
}