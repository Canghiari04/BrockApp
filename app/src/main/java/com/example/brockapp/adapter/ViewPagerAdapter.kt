package com.example.brockapp.adapter

import com.example.brockapp.NUM_TABS
import com.example.brockapp.fragment.WalkFragment
import com.example.brockapp.fragment.StillFragment
import com.example.brockapp.fragment.VehicleFragment

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                StillFragment()
            }

            1 -> {
                VehicleFragment()
            }

            else -> {
                WalkFragment()
            }
        }
    }
}