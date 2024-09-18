package com.example.brockapp.adapter

import com.example.brockapp.*

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.brockapp.fragment.ActivityFragment
import com.example.brockapp.fragment.FriendMapFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ActivityFragment()
            }

            1 -> {
                FriendMapFragment()
            }

            else -> {
                ActivityFragment()
            }
        }
    }
}