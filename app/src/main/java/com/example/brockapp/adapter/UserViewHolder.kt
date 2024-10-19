package com.example.brockapp.adapter

import com.example.brockapp.R

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubscriberViewHolder(itemView: View, private val onItemClick: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
    private val usernameTextView: TextView = itemView.findViewById(R.id.text_view_subscriber_name)
    private val addressTextView: TextView = itemView.findViewById(R.id.text_view_subscriber_address)
    private val viewFriendActivityButton: TextView = itemView.findViewById(R.id.text_view_view_friend)

    fun bindFriend (username: String, country: String?, city: String?) {
        usernameTextView.text = username
        addressTextView.text = defineSubscriberAddress(country, city)

        viewFriendActivityButton.setOnClickListener {
            onItemClick(username)
        }
    }

    private fun defineSubscriberAddress(country: String?, city: String?): String {
        return when {
            !country.isNullOrBlank() && !city.isNullOrBlank() -> "$country, $city"
            !country.isNullOrBlank() -> "$country"
            !city.isNullOrBlank() -> "$city"
            else -> ""
        }
    }
}