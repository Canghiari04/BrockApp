package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class FriendsAdapter(private val friends: List<String>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<FriendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val friendItem = LayoutInflater.from(parent.context).inflate(R.layout.friend_cell, parent, false)

        return FriendViewHolder(friendItem, onItemClick)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindFriend(friends[position])
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}