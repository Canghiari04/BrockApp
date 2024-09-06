package com.example.brockapp.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class FriendViewHolder(itemView: View, private val onItemClick: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
    private val usernameTextView: TextView = itemView.findViewById(R.id.friend_name_text_view)
    private val viewFriendActivityButton: Button = itemView.findViewById(R.id.button_view_friend)

    fun bindFriend (friend: String) {
        usernameTextView.text = friend

        viewFriendActivityButton.setOnClickListener {
            onItemClick(friend)
        }
    }
}