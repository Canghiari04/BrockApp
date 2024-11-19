package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.adapter.ViewPagerGroupAdapter

import android.view.View
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class GroupFragment: Fragment(R.layout.fragment_group) {

    private val tabsTitleArray = mapOf(
        0 to "Subscribers",
        1 to "Friends"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerGroupAdapter(childFragmentManager, lifecycle)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_group_fragment)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager_group_fragment)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(tabsTitleArray[position]!!)
        }.attach()
    }
}