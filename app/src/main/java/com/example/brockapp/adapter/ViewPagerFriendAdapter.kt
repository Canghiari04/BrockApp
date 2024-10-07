package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.page.FriendGeofencePage
import com.example.brockapp.page.FriendProgressPage

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.brockapp.data.Friend

class ViewPagerFriendAdapter(private val friend: Friend, fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS_YOU_FRIEND_PAGER
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