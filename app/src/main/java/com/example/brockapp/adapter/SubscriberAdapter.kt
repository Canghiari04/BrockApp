package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.data.Friend

class FriendAdapter(private val friends: List<Friend>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<FriendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val friendItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_friend, parent, false)
        return FriendViewHolder(friendItem, onItemClick)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindFriend(friends[position].username)
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}