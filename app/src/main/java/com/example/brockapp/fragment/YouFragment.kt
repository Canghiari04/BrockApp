package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.adapter.ViewPagerYouAdapter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class YouFragment: Fragment(R.layout.fragment_you) {
    private val tabsTitleArray = mapOf(
        0 to "Progress",
        1 to "Areas"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerYouAdapter(childFragmentManager, lifecycle)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_you_fragment)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager_you_fragment)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(tabsTitleArray[position]!!)
        }.attach()
    }
}