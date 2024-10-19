package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.data.User

class SubscriberAdapter(private val subscribers: List<User>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<SubscriberViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriberViewHolder {
        val friendItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_subscriber, parent, false)
        return SubscriberViewHolder(friendItem, onItemClick)
    }

    override fun onBindViewHolder(holder: SubscriberViewHolder, position: Int) {
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