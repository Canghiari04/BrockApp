package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.viewmodel.MemoViewModel

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyActivityViewHolder(itemView: View, private val viewModel: MemoViewModel): RecyclerView.ViewHolder(itemView) {
    val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_memo)

    private val titleTextView = itemView.findViewById<TextView>(R.id.text_view_memo_title)
    private val descriptionTextView = itemView.findViewById<TextView>(R.id.text_view_memo_description)

    // How can I reload the page? --> Do it like on iPhone
    fun bind(title: String, description: String) {
        titleTextView.text = title
        descriptionTextView.text = description
    }
}