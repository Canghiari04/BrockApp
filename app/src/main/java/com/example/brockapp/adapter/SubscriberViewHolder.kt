package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendViewHolder(itemView: View, private val onItemClick: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
    private val usernameTextView: TextView = itemView.findViewById(R.id.text_view_friend_name)
    private val viewFriendActivityButton: TextView = itemView.findViewById(R.id.text_view_view_friend)

    fun bindFriend (friend: String) {
        usernameTextView.text = friend

        viewFriendActivityButton.setOnClickListener {
            onItemClick(friend)
        }
    }
}