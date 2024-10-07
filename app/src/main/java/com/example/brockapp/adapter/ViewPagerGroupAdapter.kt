package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.page.ClubPage
import com.example.brockapp.page.FriendPage

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerGroupAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS_YOU_GROUP_PAGER
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FriendPage()
            }

            else -> {
                ClubPage()
            }
        }
    }
}