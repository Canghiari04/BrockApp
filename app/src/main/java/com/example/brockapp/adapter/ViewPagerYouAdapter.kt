package com.example.brockapp.adapter

import com.example.brockapp.*
import com.example.brockapp.page.PageActivity
import com.example.brockapp.page.PageGeofence
import com.example.brockapp.page.PageProgress

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerYouAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS_YOU
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PageProgress()
            }

            1 -> {
                PageActivity()
            }

            else -> {
                PageGeofence()
            }
        }
    }
}