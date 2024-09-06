package com.example.brockapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R

class SuggestionsAdapter(private val usernames: List<String>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<SuggestionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_suggestion, parent, false)
        return SuggestionViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bindSuggestions(usernames[position])
    }

    override fun getItemCount(): Int {
        return usernames.size
    }
}
