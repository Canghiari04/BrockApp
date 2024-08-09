package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.dialog.AccountDialog

import android.util.Log
import android.os.Bundle
import android.content.Intent
import android.app.AlertDialog
import kotlinx.coroutines.launch
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity

class MoreActivity : AppCompatActivity() {
    private val user = User.getInstance()
    private val db = BrockDB.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.more_activity)

        findViewById<CardView>(R.id.card_view_info).setOnClickListener {
            AccountDialog().show(supportFragmentManager, "CUSTOM_ACCOUNT_DIALOG")
        }

        findViewById<CardView>(R.id.card_view_logout).setOnClickListener {
            user.logoutUser(user)

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<CardView>(R.id.card_view_delete).setOnClickListener {
            showDangerousDialog(User.getInstance())
        }
    }

    private fun showDangerousDialog(user: User) {
        val userDao = db.UserDao()

        AlertDialog.Builder(this)
            .setTitle(R.string.dangerous_dialog_title)
            .setMessage(R.string.dangerous_dialog_message)
            .setPositiveButton(R.string.dangerous_positive_button) { dialog, _ ->
                dialog.dismiss()
                user.logoutUser(user)
                lifecycleScope.launch {
                    try {
                        userDao.deleteUserById(user.id)
                    } catch (e: Exception) {
                        Log.d("WTF", "WTF")
                    }
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.dangerous_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}