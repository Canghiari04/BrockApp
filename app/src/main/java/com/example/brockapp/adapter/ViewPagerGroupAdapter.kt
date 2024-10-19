package com.example.brockapp.adapter

import com.example.brockapp.page.userlist.FriendsPage
import com.example.brockapp.page.userlist.SubscribersPage

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerGroupAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                SubscribersPage()
            }

            else -> {
                FriendsPage()
            }
        }
    }
}