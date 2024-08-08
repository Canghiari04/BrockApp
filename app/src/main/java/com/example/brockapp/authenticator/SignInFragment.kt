package com.example.brockapp.authenticator

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.BLANK_ERROR
import com.example.brockapp.SIGN_IN_ERROR
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.UserEntity
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.activity.AuthenticatorActivity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope

class SignInFragment : Fragment(R.layout.sign_in_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val userDao = db.UserDao()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            val username: String = view.findViewById<EditText>(R.id.text_username).text.toString()
            val password: String = view.findViewById<EditText>(R.id.text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val userAlreadyExists = withContext(Dispatchers.IO) {
                        userDao.checkIfUserIsPresent(username, password)
                    }

                    if(userAlreadyExists){
                        Toast.makeText(requireContext(), SIGN_IN_ERROR, Toast.LENGTH_LONG).show()
                    } else {
                        withContext(Dispatchers.IO) {
                            userDao.insertUser(UserEntity(username = username, password = password))
                        }

                        val user = User.getInstance()

                        user.id = withContext(Dispatchers.IO) {
                            userDao.getIdFromUsernameAndPassword(username, password)
                        }
                        user.username = username
                        user.password = password

                        startActivity(Intent(activity, PageLoaderActivity::class.java))
                    }
                }
            } else {
                Toast.makeText(requireContext(), BLANK_ERROR, Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.login_text_view).setOnClickListener {
            startActivity(Intent(activity, AuthenticatorActivity::class.java).putExtra("TYPE_PAGE", "LogIn"))
        }
    }
}