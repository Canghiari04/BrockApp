package com.example.brockapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.os.PersistableBundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.brockapp.authenticator.SignInFragment

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.home_activity) // Set content view first

        val userData = getUserData(this)
        val userName = userData["username"] as? String
        val welcomeMessage = "Welcome, $userName!"

        val welcomeTextView = findViewById<TextView>(R.id.welcome_text) // Now find the view
        welcomeTextView.text = welcomeMessage
    }
    fun getUserData(context: Context): Map<String, *> {
        val sharedPrefs = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        return sharedPrefs.all
    }
}