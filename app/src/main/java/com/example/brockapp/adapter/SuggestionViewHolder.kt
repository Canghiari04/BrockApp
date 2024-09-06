package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionViewHolder(itemView: View, private val onItemClick: (String) -> Unit): RecyclerView.ViewHolder(itemView){
    private val usernameTextView: TextView = itemView.findViewById(R.id.user_name_text_view)

    fun bindSuggestions(username: String) {
        usernameTextView.text = username

        itemView.findViewById<Button>(R.id.follow_user_button).setOnClickListener {
            onItemClick(username)
        }
    }
}