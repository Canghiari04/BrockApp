package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.viewModel.MemoViewModel
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewModel.MemoViewModelFactory

import android.view.View
import android.os.Bundle
import java.time.Instant
import java.time.ZoneOffset
import android.view.MenuItem
import android.widget.Button
import android.content.Intent
import android.widget.Spinner
import android.widget.EditText
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class NewMemoActivity: AppCompatActivity() {
    private val toastUtil = ShowCustomToastImpl()

    private lateinit var date: String
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var typeActivity: String
    private lateinit var titleTextView: EditText
    private lateinit var viewModel: MemoViewModel
    private lateinit var descriptionTextView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo)

        date = intent.getStringExtra("CALENDAR_DATE") ?: ""
        title = intent.getStringExtra("TITLE_MEMO") ?: ""
        description = intent.getStringExtra("DESCRIPTION_MEMO") ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_memo_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleTextView = findViewById(R.id.edit_text_title)
        descriptionTextView = findViewById(R.id.edit_text_description)
        setUpEditText()

        val spinner = findViewById<Spinner>(R.id.spinner_activity_type)
        setUpSpinnerActivity(spinner)

        val idMemo = intent.getLongExtra("ID_MEMO", 0L)
        val button = findViewById<Button>(R.id.button_add_memo)
        setUpButton(idMemo, button)

        val db = BrockDB.getInstance(this)
        val viewModelFactory = MemoViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory)[MemoViewModel::class.java]
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, DailyMemoActivity::class.java).putExtra("CALENDAR_DATE", date)
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setUpEditText() {
        titleTextView.setText(title)
        descriptionTextView.setText(description)
    }

    private fun setUpSpinnerActivity(spinner: Spinner) {
        val spinnerItems = resources.getStringArray(R.array.spinner_activities)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                typeActivity = spinnerItems[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    private fun setUpButton(id: Long, button: Button) {
        if (id == 0L) {
            button.also {
                it.text = resources.getText(R.string.button_add_memo)
                it.setOnClickListener {
                    if (!titleTextView.text.isNullOrBlank() && !descriptionTextView.text.isNullOrBlank()) {
                        insertMemo()
                    } else {
                        toastUtil.showWarningToast(
                            "You must insert the field required",
                            this
                        )
                    }
                }
            }
        } else {
            button.also {
                it.text = resources.getText(R.string.button_modify_memo)
                it.setOnClickListener {
                    if (!titleTextView.text.isNullOrBlank() && !descriptionTextView.text.isNullOrBlank()) {
                        updateMemo(id)
                    } else {
                        toastUtil.showWarningToast(
                            "You must inserted the field required",
                            this
                        )
                    }
                }
            }
        }
    }

    private fun insertMemo() {
        val timestamp = DateTimeFormatter
            .ofPattern(ISO_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        val memoEntity = MemosEntity(
            username = MyUser.username,
            title = titleTextView.text.toString(),
            description = descriptionTextView.text.toString(),
            activityType = typeActivity,
            date = date,
            timestamp = timestamp
        )

        titleTextView.text.clear()
        descriptionTextView.text.clear()

        viewModel.insertMemo(memoEntity)
    }

    private fun updateMemo(id: Long) {
        viewModel.updateMemo(
            id,
            titleTextView.text.toString(),
            descriptionTextView.text.toString(),
            typeActivity
        )
    }
}