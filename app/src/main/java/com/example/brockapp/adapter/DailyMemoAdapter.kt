package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.database.MemoEntity

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class DailyMemoAdapter(private val list: List<MemoEntity>): RecyclerView.Adapter<DailyMemoViewHolder>() {
    private val selectedMemos = mutableListOf<MemoEntity>()

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
    }

    fun getMemosSelected(): List<MemoEntity> {
        return selectedMemos
    }
}