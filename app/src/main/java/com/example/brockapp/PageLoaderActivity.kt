package com.example.brockapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.ActivitiesFragment

class PageLoaderActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.page_loader_activity)



        val pageType = intent.getStringExtra("TYPE_PAGE").toString()

        when (pageType) {
            "activities" -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.activities_fragment, ActivitiesFragment())
                    commit()
                }
            }
//            "calendar" -> {
//                supportFragmentManager.beginTransaction().apply {
//                    replace(R.id.auth_fragment, CalendarFragment()) // Replace with your actual fragment
//                    commit()
//                }
//            }
//            "history" -> {
//                supportFragmentManager.beginTransaction().apply {
//                    replace(R.id.auth_fragment, HistoryFragment()) // Replace with your actual fragment
//                    commit()
//                }
//            }
//            "friends" -> {
//                supportFragmentManager.beginTransaction().apply {
//                    replace(R.id.auth_fragment, FriendsFragment()) // Replace with your actual fragment
//                    commit()
//                }
//            }
            else -> {
                System.out.println("Page not found")
            }
        }
    }
}