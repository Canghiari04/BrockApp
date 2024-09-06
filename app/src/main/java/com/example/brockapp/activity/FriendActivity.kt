package com.example.brockapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.brockapp.R
import com.example.brockapp.adapter.ViewPagerAdapter
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File

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

        val s3Client = S3ClientProvider.getInstance(this)

        val file = File(this.filesDir, "user_data.json")

        val db = BrockDB.getInstance(this)
        val viewModelFactory = FriendsViewModelFactory(s3Client, db, file)
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