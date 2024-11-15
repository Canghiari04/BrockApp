package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.viewmodel.FriendsViewModel

import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.widget.Button
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment

class NewFriendDialog(private val username: String, private val viewModel: FriendsViewModel): DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_new_friend_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.text_dialog_new_friend)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        textView.setText("Desideri aggiungere $username alla tua lista di amici?")

        view.findViewById<Button>(R.id.add_new_friend_button).setOnClickListener {
            viewModel.addFriend(username)
            dismiss()
        }

        view.findViewById<Button>(R.id.dismiss_dialog_button).setOnClickListener {
            dismiss()
        }
    }
}