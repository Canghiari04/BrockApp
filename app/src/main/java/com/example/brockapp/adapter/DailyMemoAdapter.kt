package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.activity.NewMemoActivity

import android.content.Intent
import android.view.ViewGroup
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class DailyMemoAdapter(private val date: String?, private val list: List<MemosEntity>, private val context: Context): RecyclerView.Adapter<DailyMemoViewHolder>() {
    private val selectedMemos = mutableListOf<MemosEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyMemoViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_memo, parent, false)
        return DailyMemoViewHolder(activityItem)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DailyMemoViewHolder, position: Int) {
        val memo = list[position]

        holder.bind(memo.title, memo.description, memo.activityType)

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedMemos.add(memo)
            } else {
                selectedMemos.remove(memo)
            }
        }

        holder.button.setOnClickListener {
            val title = holder.titleTextView.text
            val description = holder.descriptionTextView.text

            Intent(context, NewMemoActivity::class.java).also {
                it.putExtra("CALENDAR_DATE", date)

                it.putExtra("ID_MEMO", memo.id)
                it.putExtra("TITLE_MEMO", title)
                it.putExtra("DESCRIPTION_MEMO", description)

                context.startActivity(it)
            }
        }
    }

    fun getMemosSelected(): List<MemosEntity> {
        return selectedMemos
    }
}