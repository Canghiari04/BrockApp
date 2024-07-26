package com.example.brockapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PageLoaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_loader_activity)

        val pageType = intent.getStringExtra("TYPE_PAGE").toString()

        when (pageType) {
            "activities" -> {
                // Utilizza 'this' come Context e specifica l'attività da avviare
                val intent = Intent(this, DetectActivity::class.java)
                startActivity(intent)
            }
            "calendar" -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.page_loader_activity, CalendarFragment()) // Sostituisci con il tuo Fragment
                    commit()
                }
            }
            "history" -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.page_loader_activity, HistoryFragment()) // Sostituisci con il tuo Fragment
                    commit()
                }
            }
            "friends" -> {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.page_loader_activity, ChartsFragment()) // Sostituisci con il tuo Fragment
                    commit()
                }
            }
            else -> {
                System.out.println("Page not found")
            }
        }
    }
}
