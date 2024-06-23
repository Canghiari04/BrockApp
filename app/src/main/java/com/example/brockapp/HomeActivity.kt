package com.example.brockapp

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument.Page
import android.os.Bundle
import android.view.View
import android.os.PersistableBundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.brockapp.authenticator.SignInFragment

class HomeActivity : AppCompatActivity() {

    companion object {
        const val ACTIVITIES = "activities"
        const val CALENDAR = "calendar"
        const val HISTORY = "history"
        const val FRIENDS = "friends"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.home_activity) // Set content view first

        val userData = getUserData(this)
        val userName = userData["username"] as? String
        val welcomeMessage = "Welcome, $userName!"

        val welcomeTextView = findViewById<TextView>(R.id.welcome_text) // Now find the view
        welcomeTextView.text = welcomeMessage

        findViewById<CardView>(R.id.historical_card).setOnClickListener {
            loadPage(HISTORY)
        }

        findViewById<CardView>(R.id.activity_card).setOnClickListener {
            loadPage(ACTIVITIES)
        }

        findViewById<CardView>(R.id.calendar_card).setOnClickListener {
            loadPage(CALENDAR)
        }

        findViewById<CardView>(R.id.friends_card).setOnClickListener {
            loadPage(FRIENDS)
        }
    }
    private fun getUserData(context: Context): Map<String, *> {
        val sharedPrefs = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        return sharedPrefs.all
    }

    private fun loadPage(typeFragment : String) {
        startActivity(Intent(this, PageLoaderActivity::class.java).putExtra("TYPE_PAGE", typeFragment))
    }
}