package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyMemoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_memo)
    val button: Button = itemView.findViewById(R.id.button_modify_memo)
    val titleTextView: TextView = itemView.findViewById(R.id.text_view_memo_title)
    val descriptionTextView: TextView = itemView.findViewById(R.id.text_view_memo_description)

    private val typeActivityTextView: TextView = itemView.findViewById(R.id.text_view_type_activity)

    fun bind(title: String, description: String, activityType: String) {
        titleTextView.text = title
        descriptionTextView.text = description
        typeActivityTextView.text = "Type activity associated " + activityType
    }
}