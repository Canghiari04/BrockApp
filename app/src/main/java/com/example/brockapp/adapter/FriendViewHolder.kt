package com.example.brockapp.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val usernameTextView: TextView = view.findViewById(R.id.friend_name_text_view)
}