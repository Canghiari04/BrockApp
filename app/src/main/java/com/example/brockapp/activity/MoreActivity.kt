package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.dialog.AccountDialog

import android.os.Bundle
import android.content.Intent
import android.app.AlertDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity

class MoreActivity : AppCompatActivity() {
    private val db = BrockDB.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.more_activity)

        findViewById<CardView>(R.id.card_view_info).setOnClickListener {
            AccountDialog().show(supportFragmentManager, "CUSTOM_ACCOUNT_DIALOG")
        }

        findViewById<CardView>(R.id.card_view_logout).setOnClickListener {
            User.getInstance().logoutUser(User.getInstance())

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<CardView>(R.id.card_view_delete).setOnClickListener {
            showDangerousDialog(User.getInstance())
        }
    }

    private fun showDangerousDialog(user: User) {
        val userId = user.id
        val userDao = db.UserDao()

        AlertDialog.Builder(this)
            .setTitle(R.string.dangerous_dialog_title)
            .setMessage(R.string.dangerous_dialog_message)
            .setPositiveButton(R.string.dangerous_positive_button) { dialog, _ ->
                dialog.dismiss()
                user.logoutUser(user)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        userDao.deleteUserById(userId)
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