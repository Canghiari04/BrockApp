package com.example.brockapp.activity

import com.example.brockapp.R

import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.google.android.material.tabs.TabLayout
import com.example.brockapp.adapter.ViewPagerAdapter
import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class FriendActivity: AppCompatActivity() {
    private val tabsIconArray = mapOf(
        0 to R.drawable.baseline_chair_24,
        1 to R.drawable.baseline_directions_car_24,
        2 to R.drawable.baseline_directions_walk_24
    )

    private lateinit var viewModel: FriendsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_friend_activity)
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.friends_tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.friends_view_pager)

        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val friendUsername = intent.getStringExtra("FRIEND_USERNAME")

        val credentialsProvider = CognitoCachingCredentialsProvider(this, "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00", Regions.EU_WEST_3)
        val s3Client = AmazonS3Client(credentialsProvider)

        val db = BrockDB.getInstance(this)
        val viewModelFactory = FriendsViewModelFactory(s3Client, db, this)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendsViewModel::class.java]

        viewModel.getFriendActivities(friendUsername)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(tabsIconArray[position]!!)
        }.attach()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", "Amici")
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }
}