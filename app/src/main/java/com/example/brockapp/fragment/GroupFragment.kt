package com.example.brockapp.fragment

import com.example.brockapp.R

import android.view.View
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.example.brockapp.adapter.ViewPagerGroupAdapter
import com.google.android.material.tabs.TabLayoutMediator

class GroupFragment: Fragment(R.layout.page_friends) {
    private val tabsTitleArray = mapOf(
        0 to "Friend",
        1 to "Club"
    )

    private val tabsIconArray = mapOf(
        0 to R.drawable.baseline_emoji_people_24,
        1 to R.drawable.baseline_groups_24
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerGroupAdapter(childFragmentManager, lifecycle)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_group)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_page_group)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(tabsTitleArray[position]!!)
            tab.setIcon(tabsIconArray[position]!!)
        }.attach()
    }
}