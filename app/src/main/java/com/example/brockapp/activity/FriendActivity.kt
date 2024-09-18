package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.adapter.ViewPagerAdapter
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.FriendsViewModelFactory

import java.io.File
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FriendActivity: AppCompatActivity() {
    private val tabsTitleArray = mapOf(
        0 to R.drawable.baseline_directions_run_24,
        1 to R.drawable.marker_icon,
    )

    private lateinit var viewModel: FriendsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_friend_activity)
        setSupportActionBar(toolbar)

        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        val tabLayout = findViewById<TabLayout>(R.id.friends_tab_layout)

        val viewPager = findViewById<ViewPager2>(R.id.friends_view_pager)
        viewPager.adapter = adapter

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = S3ClientProvider.getInstance(this)

        val viewModelFactory = FriendsViewModelFactory(s3Client, db, file)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendsViewModel::class.java]

        val friendUsername = intent.getStringExtra("FRIEND_USERNAME")
        viewModel.getFriendData(friendUsername)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(tabsTitleArray[position]!!)
        }.attach()
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