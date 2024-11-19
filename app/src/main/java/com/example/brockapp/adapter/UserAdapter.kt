package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.User

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val list: List<User>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View, private val onItemClick: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
        val button: TextView = itemView.findViewById(R.id.text_view_view_friend)
        val username: TextView = itemView.findViewById(R.id.text_view_subscriber_name)
        val address: TextView = itemView.findViewById(R.id.text_view_subscriber_address)

        fun setupViewHolder (username: String) {
            button.setOnClickListener {
                onItemClick(username)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.cell_user, parent, false)
        return UserViewHolder(item, onItemClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = list[position]

        holder.username.text = item.username
        holder.address.text = defineSubscriberAddress(item.city, item.country)

        holder.setupViewHolder(item.username)
    }

    private fun defineSubscriberAddress(city: String?, country: String?): String {
        return when {
            !country.isNullOrBlank() && !city.isNullOrBlank() -> "$country, $city"
            !country.isNullOrBlank() -> "$country"
            !city.isNullOrBlank() -> "$city"
            else -> ""
        }
    }
}