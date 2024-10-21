package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.data.User

class UserAdapter(private val subscribers: List<User>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val friendItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_user, parent, false)
        return UserViewHolder(friendItem, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bindFriend(
            subscribers[position].username,
            subscribers[position].country,
            subscribers[position].city
        )
    }

    override fun getItemCount(): Int {
        return subscribers.size
    }
}