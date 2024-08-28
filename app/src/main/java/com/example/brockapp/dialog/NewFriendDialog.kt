package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.FriendEntity
import com.example.brockapp.viewmodel.FriendsViewModel

import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.widget.Button
import android.view.ViewGroup
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
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

        val user = User.getInstance()
        val db = BrockDB.getInstance(requireContext())

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<Button>(R.id.add_new_friend_button).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val friend = FriendEntity(userId = user.id, followedUsername = username)
                db.FriendDao().insertFriend(friend)
            }

            viewModel.addFriend(username)
            dismiss()
        }

        view.findViewById<Button>(R.id.dismiss_dialog_button).setOnClickListener {
            dismiss()
        }
    }
}