package com.example.brockapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.FriendEntity
import com.example.brockapp.singleton.User
import com.example.brockapp.viewmodel.FriendsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsersAdapter(val newUser : String, val viewModel: FriendsViewModel): RecyclerView.Adapter<UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_cell, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.usernameTextView.text = newUser


        holder.followButton.setOnClickListener {

            viewModel.addFriend(newUser)
            val db = BrockDB.getInstance(holder.usernameTextView.context)
            CoroutineScope(Dispatchers.IO).launch {

                val friend = FriendEntity(userId = User.id, followedUsername = newUser)
                db.FriendDao().insertFriend(friend)
            }

        }
    }


    override fun getItemCount(): Int = 1

}
