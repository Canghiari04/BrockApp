package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.database.DbHelper

import android.os.Bundle
import android.content.Intent
import android.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.dialog.AccountDialog

class MoreActivity : AppCompatActivity() {
    private val user = User.getInstance()
    private val dbHelper = DbHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.more_activity)

        findViewById<CardView>(R.id.card_view_info).setOnClickListener {
            AccountDialog().show(supportFragmentManager, "CUSTOM_ACCOUNT_DIALOG")
        }

        findViewById<CardView>(R.id.card_view_logout).setOnClickListener {
            user.logoutUser(user)

            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<CardView>(R.id.card_view_delete).setOnClickListener {
            showDangerousDialog(User.getInstance())
        }
    }

    private fun showDangerousDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dangerous_dialog_title)
            .setMessage(R.string.dangerous_dialog_message)
            .setPositiveButton(R.string.dangerous_positive_button) { dialog, _ ->
                dialog.dismiss()
                user.logoutUser(user)
                dbHelper.deleteUser(user.id)
                startActivity(Intent(this, MainActivity::class.java))
            }
            .setNegativeButton(R.string.dangerous_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}