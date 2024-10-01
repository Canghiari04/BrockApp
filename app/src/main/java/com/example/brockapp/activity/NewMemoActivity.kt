package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.*
import com.example.brockapp.`object`.MyUser
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.MemoEntity
import com.example.brockapp.viewmodel.MemoViewModel
import com.example.brockapp.viewmodel.MemoViewModelFactory

import android.view.View
import android.os.Bundle
import java.time.Instant
import java.time.ZoneOffset
import android.widget.Toast
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
    private lateinit var date: String
    private lateinit var typeActivity: String
    private lateinit var viewModel: MemoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo)

        date = intent.getStringExtra("CALENDAR_DATE").toString()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_new_memo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        val spinner = findViewById<Spinner>(R.id.spinner_activity_type)
        setUpSpinnerActivity(spinner)

        val button = findViewById<Button>(R.id.button_add_memo)
        setUpButtonAddMemo(button)

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

    private fun setUpSpinnerActivity(spinner: Spinner) {
        val spinnerItems = resources.getStringArray(R.array.spinner_activity)

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

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun setUpButtonAddMemo(button: Button) {
        button.setOnClickListener {
            val titleTextView = findViewById<EditText>(R.id.edit_text_title)
            val descriptionTextView = findViewById<EditText>(R.id.edit_text_description)
            val timestamp = DateTimeFormatter
                .ofPattern(ISO_DATE_FORMAT)
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())

            if (typeActivity.isEmpty() && titleTextView.text.toString().isEmpty() && descriptionTextView.text.toString().isEmpty()) {
                Toast.makeText(this, "You must insert something about the memo", Toast.LENGTH_SHORT).show()
            } else {
                val memoEntity = MemoEntity(
                    userId = MyUser.id,
                    title = titleTextView.text.toString(),
                    description = descriptionTextView.text.toString(),
                    activityType = typeActivity,
                    date = date,
                    timestamp = timestamp
                )

                titleTextView.setText("")
                descriptionTextView.setText("")

                viewModel.insertMemo(memoEntity)
            }
        }
    }
}