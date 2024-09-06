package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.singleton.User

import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.widget.Button
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment

class AccountDialog: DialogFragment() {
    private val user = User.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_account_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameTextView = view.findViewById<TextView>(R.id.account_username_text_view)
        val passwordTextView = view.findViewById<TextView>(R.id.account_password_text_view)

        val entryUsername = usernameTextView.text.toString()
        val entryPassword = passwordTextView.text.toString()

        val accountUsername = entryUsername + " ${user.username}"
        val accountPassword = entryPassword + " ${user.password}"

        usernameTextView.text = accountUsername
        passwordTextView.text = accountPassword

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<Button>(R.id.dismiss_dialog_button).setOnClickListener {
            dismiss()
        }
    }
}