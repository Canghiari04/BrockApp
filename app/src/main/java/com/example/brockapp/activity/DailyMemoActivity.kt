package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.viewmodel.MemoViewModel
import com.example.brockapp.adapter.DailyMemoAdapter
import com.example.brockapp.interfaces.ScheduleWorkerImpl
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
    private var date: String? = null

    private val toastUtil = ShowCustomToastImpl()

    private lateinit var viewModel: MemoViewModel
    private lateinit var adapter: DailyMemoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var button: FloatingActionButton
    private lateinit var scheduleWorkerUtil: ScheduleWorkerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_memo)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_daily_memo_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.text_blank)

        date = intent.getStringExtra("CALENDAR_DATE")

        val db = BrockDB.getInstance(this)
        val viewModelFactory = MemoViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory)[MemoViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view_memos)

        button = findViewById(R.id.button_new_memo)
        setUpFloatingButton(date)

        scheduleWorkerUtil = ScheduleWorkerImpl(this)

        observeMemos()

        viewModel.getMemos(date!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra(
                    "FRAGMENT_TO_SHOW",
                    R.id.navbar_item_calendar
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
            adapter.getMemosSelected().forEach {
                viewModel.deleteMemo(it)
                scheduleWorkerUtil.scheduleDeleteMemoWorker(it.id)
            }
        } else {
            Log.d("DAILY_MEMO_ACTIVITY", "No one memo inside the list")
        }
    }

    private fun setUpFloatingButton(date: String?) {
        button.setOnClickListener {
            val intent = Intent(this, NewMemoActivity::class.java).putExtra("CALENDAR_DATE", date)
            startActivity(intent)
            finish()
        }

        if (!checkCurrentDate(date)) {
            button.hide()
        }
    }

    private fun checkCurrentDate(date: String?): Boolean {
        val currentDate = LocalDate.now()
        val dailyDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT))

        return currentDate.isEqual(dailyDate) || currentDate.isBefore(dailyDate)
    }
    
    private fun observeMemos() {
        viewModel.memos.observe(this) { list ->
            if (!list.isNullOrEmpty()) {
                populateRecyclerView(list)
            } else {
                toastUtil.showBasicToast(
                    "No one memo retrieved",
                    this
                )

                if (!checkCurrentDate(date)) {
                    setContentView(R.layout.activity_empty_page)

                    val toolbar = findViewById<Toolbar>(R.id.toolbar_empty_activity)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    setSupportActionBar(toolbar)
                }
            }
        }
    }

    private fun populateRecyclerView(list: List<MemosEntity>) {
        adapter = DailyMemoAdapter(date, list, this)
        val layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }
}