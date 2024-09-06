package com.example.brockapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class FriendsAdapter(private val friends: List<String>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<FriendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val friendItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_friend, parent, false)
        return FriendViewHolder(friendItem, onItemClick)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindFriend(friends[position])
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}