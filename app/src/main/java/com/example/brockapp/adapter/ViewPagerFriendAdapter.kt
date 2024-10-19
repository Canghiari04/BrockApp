package com.example.brockapp.adapter

import com.example.brockapp.data.Friend
import com.example.brockapp.page.friend.FriendProgressPage
import com.example.brockapp.page.friend.FriendGeofencePage

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerFriendAdapter(private val friend: Friend, fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FriendProgressPage(friend)
            }

            else -> {
                FriendGeofencePage(friend)
            }
        }
    }
}