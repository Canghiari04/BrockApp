package com.example.brockapp

import android.content.ClipData.Item
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity

class PageLoaderActivity : ComponentActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        /*
         * Dall'intent acquisisco la tipologia di fragment che dovrà essere sovrapposta.
         */
        findViewById<View>(R.id.bottom_navigation_view)
        when (intent.getStringExtra("TYPE_PAGE").toString()) {
            "home" -> {
                startActivity(Intent(this, DetectActivity::class.java))
            }
            "activities" -> {
                startActivity(Intent(this, DetectActivity::class.java))
            }
            "calendar" -> {
                startActivity(Intent(this, DetectActivity::class.java))
            }
            "history" -> {
                startActivity(Intent(this, DetectActivity::class.java))
            }
            "friends" -> {
                startActivity(Intent(this, DetectActivity::class.java))
            }
            else -> {
                System.out.println("Page not found")
            }
        }
    }
}