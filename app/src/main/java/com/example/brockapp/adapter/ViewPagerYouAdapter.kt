package com.example.brockapp.adapter

import com.example.brockapp.page.you.YouGeofencePage
import com.example.brockapp.page.you.YouProgressPage

import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerYouAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                YouProgressPage()
            }

            else -> {
                YouGeofencePage()
            }
        }
    }
}