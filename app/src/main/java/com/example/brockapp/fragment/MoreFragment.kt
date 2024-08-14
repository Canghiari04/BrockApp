package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.dialog.AccountDialog

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.app.AlertDialog
import android.util.Log
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.cardview.widget.CardView
import com.example.brockapp.activity.AuthenticatorActivity

class MoreFragment(): Fragment(R.layout.more_fragment) {
    private val user = User.getInstance()
    private val db = BrockDB.getInstance(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<CardView>(R.id.card_view_info).setOnClickListener {
            AccountDialog().show(requireActivity().supportFragmentManager, "CUSTOM_ACCOUNT_DIALOG")
        }

        view.findViewById<CardView>(R.id.card_view_logout).setOnClickListener {
            user.logoutUser(user)
            startActivity(Intent(requireContext(), AuthenticatorActivity::class.java))
            requireActivity().finish()
        }

        view.findViewById<CardView>(R.id.card_view_delete).setOnClickListener {
            showDangerousDialog(User.getInstance())
        }
    }

    private fun showDangerousDialog(user: User) {
        val db = BrockDB.getInstance(requireContext())
        val userDao = db.UserDao()

        AlertDialog.Builder(requireContext())
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
                startActivity(Intent(requireContext(), AuthenticatorActivity::class.java))
            }
            .setNegativeButton(R.string.dangerous_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}