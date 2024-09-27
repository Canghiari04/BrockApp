package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.util.CalendarUtil
import com.example.brockapp.database.MemoEntity
import com.example.brockapp.viewmodel.MemoViewModel
import com.example.brockapp.adapter.DailyActivityAdapter
import com.example.brockapp.viewmodel.MemoViewModelFactory

import android.os.Bundle
import java.time.LocalDate
import android.widget.Toast
import android.view.MenuItem
import android.content.Intent
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DailyActivity: AppCompatActivity() {
    private val utilCalendar: CalendarUtil = CalendarUtil()

    private lateinit var viewModel: MemoViewModel
    private lateinit var button: FloatingActionButton
    private lateinit var adapter: DailyActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_activity)

        val date: String? = intent.getStringExtra("CALENDAR_DATE")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_daily_activity)
        setSupportActionBar(toolbar)

        val textViewDate = findViewById<TextView>(R.id.text_view_date)
        val prettyDate = utilCalendar.getPrettyDate(date)
        textViewDate.text = prettyDate

        button = findViewById(R.id.new_memo_button)

        if (checkCurrentDate(date)) {
            setUpFloatingButton(date)
        } else {
            button.hide()
        }

        val db = BrockDB.getInstance(this)
        val viewModelFactory = MemoViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory)[MemoViewModel::class.java]

        observeMemos()

        viewModel.getMemos(date!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra(
                    "FRAGMENT_TO_SHOW",
                    "Calendar"
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

    override fun onStop() {
        super.onStop()

        if (::adapter.isInitialized) {
            val list = adapter.getMemosSelected()
            list.forEach { memo ->
                viewModel.deleteMemo(memo)
            }
        } else {
            Log.d("DAILY_ACTIVITY", "None memos inside the list")
        }
    }

    private fun checkCurrentDate(date: String?): Boolean {
        val currentDate = LocalDate.now()
        val dailyDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT))

        if (currentDate.isEqual(dailyDate) || currentDate.isBefore(dailyDate)) {
            return true
        }

        return false
    }

    private fun setUpFloatingButton(date: String?) {
        // I used the date to do the right "come back" from NewMemo to DailyActivity
        button.setOnClickListener {
            val intent = Intent(this, NewMemo::class.java).putExtra("CALENDAR_DATE", date)
            startActivity(intent)
            finish()
        }
    }

    private fun observeMemos() {
        viewModel.memos.observe(this) { list ->
            if (!list.isNullOrEmpty()) {
                val recyclerView = findViewById<RecyclerView>(R.id.memos_recycler_view)
                populateRecyclerView(list, recyclerView)
            } else {
                Toast.makeText(
                    this,
                    "None memo detect",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun populateRecyclerView(list: List<MemoEntity>, recyclerView: RecyclerView) {
        adapter = DailyActivityAdapter(list, viewModel)
        val layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }
}