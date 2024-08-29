package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val usernameTextView: TextView = itemView.findViewById(R.id.user_name_text_view)
    val followButton : Button = itemView.findViewById(R.id.follow_user_button)
}