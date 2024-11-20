package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.viewModel.MemoViewModel
import com.example.brockapp.util.ScheduleWorkerUtil
import com.example.brockapp.adapter.DailyMemoAdapter
import com.example.brockapp.viewModel.MemoViewModelFactory

import java.util.Locale
import android.util.Log
import android.os.Bundle
import java.time.LocalDate
import android.content.Intent
import java.time.format.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.MaterialTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.compose.material.icons.outlined.Warning
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DailyMemoActivity: AppCompatActivity() {

    private var date: String? = null

    private lateinit var viewModel: MemoViewModel
    private lateinit var adapter: DailyMemoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var button: FloatingActionButton
    private lateinit var scheduleWorkerUtil: ScheduleWorkerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_memo)

        date = intent.getStringExtra("CALENDAR_DATE")

        val item = LocalDate.parse(
            date,
            DateTimeFormatter.ofPattern(CALENDAR_DATE_FORMAT)
        )

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "${item.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)}, ${item.month.toString().lowercase().replaceFirstChar { it.uppercase() }} ${item.dayOfMonth}"

        val db = BrockDB.getInstance(this)
        val viewModelFactory = MemoViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory)[MemoViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view_memos)

        button = findViewById(R.id.button_new_memo)
        setUpFloatingButton(date)

        scheduleWorkerUtil = ScheduleWorkerUtil(this)

        observeMemos()

        viewModel.getMemos(date!!)
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
            val intent = Intent(this, RegistrationMemoActivity::class.java).putExtra("CALENDAR_DATE", date)
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
                if (!checkCurrentDate(date)) {
                    setContentView(R.layout.activity_empty)

                    val composeView = findViewById<ComposeView>(R.id.compose_view_activity_empty)

                    composeView.setContent {
                        EmptyPage()
                    }

                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    @Composable
    fun EmptyPage() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Empty",
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                tint = Color.Gray
            )
            Text(
                text = "No memo found",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp,
                    color = Color.Gray,
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}