package com.example.brockapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.brockapp.NUM_TABS
import com.example.brockapp.fragment.StillFragment
import com.example.brockapp.fragment.VehicleFragment
import com.example.brockapp.fragment.WalkFragment

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