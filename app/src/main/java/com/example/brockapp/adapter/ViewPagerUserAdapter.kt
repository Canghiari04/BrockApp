package com.example.brockapp.adapter

import com.example.brockapp.data.Friend
import com.example.brockapp.page.user.UserProgressPage
import com.example.brockapp.page.user.UserGeofencePage

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerUserAdapter(private val friend: Friend, fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                UserProgressPage(friend)
            }

            else -> {
                UserGeofencePage(friend)
            }
        }
    }
}