package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.activity.SettingsActivity
import com.example.brockapp.adapter.ViewPagerYouAdapter
import com.example.brockapp.activity.ManualRegistrationActivity

import android.os.Bundle
import android.view.View
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.colorResource
import androidx.viewpager2.widget.ViewPager2
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.tabs.TabLayout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.material.tabs.TabLayoutMediator
import androidx.compose.material3.FloatingActionButtonDefaults

class YouFragment: Fragment(R.layout.fragment_you) {

    private val tabsTitleArray = mapOf(
        0 to "Progress",
        1 to "Areas"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerYouAdapter(childFragmentManager, lifecycle)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_you_fragment)

        val settingsButton = view.findViewById<ComposeView>(R.id.button_settings)
        settingsButton.setContent {
            DefineFloatingButton(
                "Settings",
                Icons.Filled.Settings,
            ) {
                val intent = Intent(requireContext(), SettingsActivity()::class.java)
                startActivity(intent)
            }
        }

        val registerButton = view.findViewById<ComposeView>(R.id.button_register)
        registerButton.setContent {
            DefineFloatingButton(
                "Add",
                Icons.Filled.Add,
            ) {
                val intent = Intent(requireContext(), ManualRegistrationActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager_you_fragment)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(tabsTitleArray[position]!!)
        }.attach()
    }

    @Composable
    private fun DefineFloatingButton(description: String, icon: ImageVector, onClick: () -> Unit) {
        FloatingActionButton(
            onClick = {
                onClick()
            },
            containerColor = colorResource(id = R.color.uni_red),
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = Color.White
            )
        }
    }
}