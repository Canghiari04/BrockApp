package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.viewModel.GroupViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.adapter.ViewPagerUserAdapter
import com.example.brockapp.viewModel.GroupViewModelFactory

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserActivity: AppCompatActivity() {

    private var username: String? = null

    private val tabsTitleArray = mapOf(
        0 to "Progress",
        1 to "Areas"
    )

    private lateinit var viewModel: GroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        username = intent.getStringExtra("USERNAME_SUBSCRIBER")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        val db = BrockDB.getInstance(this)
        val s3Client = MyS3ClientProvider.getInstance(this)
        val factoryViewModel = GroupViewModelFactory(s3Client, db)

        viewModel = ViewModelProvider(this, factoryViewModel)[GroupViewModel::class.java]

        observeFriend()

        viewModel.loadDataFriend(username!!)
    }

    private fun observeFriend() {
        viewModel.friend.observe(this) {
            val adapter = ViewPagerUserAdapter(it, supportFragmentManager, lifecycle)
            val tabLayout = findViewById<TabLayout>(R.id.tab_layout_user)

            val viewPager = findViewById<ViewPager2>(R.id.view_pager_user)
            viewPager.adapter = adapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setText(tabsTitleArray[position]!!)
            }.attach()
        }
    }
}