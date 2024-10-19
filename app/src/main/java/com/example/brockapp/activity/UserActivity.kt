package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.adapter.ViewPagerFriendAdapter
import com.example.brockapp.viewmodel.GroupViewModelFactory

import android.os.Bundle
import android.widget.Button
import android.view.MenuItem
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FriendActivity: AppCompatActivity() {
    private var username: String? = null
    private var toastUtil = ShowCustomToastImpl()

    private val tabsTitleArray = mapOf(
        0 to "Progress",
        1 to "Areas"
    )

    private lateinit var viewModel: GroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        // Here I don't need the view model group, but only pass the username to the fragment
        username = intent.getStringExtra("USERNAME_SUBSCRIBER")

        val toolbar = findViewById<Toolbar>(R.id.toolbar_friend_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        val db = BrockDB.getInstance(this)
        val s3Client = MyS3ClientProvider.getInstance(this)
        val factoryViewModel = GroupViewModelFactory(s3Client, db)
        viewModel = ViewModelProvider(this, factoryViewModel)[GroupViewModel::class.java]

        observeFriend()
        observeAddedFriend()

        viewModel.loadDataFriend(username!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra(
                    "FRAGMENT_TO_SHOW",
                    R.id.navbar_item_group
                    )
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

    // Only when I loaded the set of information about the friend then I will create the view pager
    private fun observeFriend() {
        viewModel.friend.observe(this) {
            val adapter = ViewPagerFriendAdapter(it, supportFragmentManager, lifecycle)
            val tabLayout = findViewById<TabLayout>(R.id.friend_tab_layout)

            val viewPager = findViewById<ViewPager2>(R.id.friend_view_pager)
            viewPager.adapter = adapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setText(tabsTitleArray[position]!!)
            }.attach()
        }
    }

    private fun observeAddedFriend() {
        viewModel.errorAddFriend.observe(this) {
            if (it) {
                toastUtil.showBasicToast(
                    "$username is your new friend",
                    this
                )
            } else {
                toastUtil.showWarningToast(
                    "Encountered error while adding",
                    this
                )
            }
        }
    }
}