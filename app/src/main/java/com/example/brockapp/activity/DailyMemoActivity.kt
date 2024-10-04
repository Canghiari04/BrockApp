package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.MemoEntity
import com.example.brockapp.viewmodel.MemoViewModel
import com.example.brockapp.adapter.DailyMemoAdapter
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewmodel.MemoViewModelFactory

import android.util.Log
import android.os.Bundle
import java.time.LocalDate
import android.view.MenuItem
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DailyMemoActivity: AppCompatActivity() {
    private val toastUtil = ShowCustomToastImpl()

    private lateinit var viewModel: MemoViewModel
    private lateinit var adapter: DailyMemoAdapter
    private lateinit var button: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_memo)

        val date: String? = intent.getStringExtra("CALENDAR_DATE")

        val toolbar = findViewById<Toolbar>(R.id.toolbar_daily_memo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        // Check if date is persistent to the current date
        button = findViewById(R.id.button_new_memo)

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

    override fun onPause() {
        super.onPause()

        if (::adapter.isInitialized) {
            adapter.getMemosSelected().forEach { memo ->
                viewModel.deleteMemo(memo)
            }
        } else {
            Log.d("DAILY_MEMO_ACTIVITY", "No one memo inside the list")
        }
    }

    private fun checkCurrentDate(date: String?): Boolean {
        val currentDate = LocalDate.now()
        val dailyDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT))

        return currentDate.isEqual(dailyDate) || currentDate.isBefore(dailyDate)
    }

    private fun setUpFloatingButton(date: String?) {
        // I'm using the date to do the right "coming back" from NewMemo to DailyMemo
        button.setOnClickListener {
            val intent = Intent(this, NewMemoActivity::class.java).putExtra("CALENDAR_DATE", date)
            startActivity(intent)
            finish()
        }
    }

    private fun observeMemos() {
        viewModel.memos.observe(this) { list ->
            if (!list.isNullOrEmpty()) {
                val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_memos)
                populateRecyclerView(list, recyclerView)
            } else {
                setContentView(R.layout.activity_empty_page)

                val toolbar = findViewById<Toolbar>(R.id.toolbar_empty_activity)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                setSupportActionBar(toolbar)

                toastUtil.showBasicToast(
                    "No one memo retrieved",
                    this
                )
            }
        }
    }

    private fun populateRecyclerView(list: List<MemoEntity>, recyclerView: RecyclerView) {
        adapter = DailyMemoAdapter(list)
        val layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }
}