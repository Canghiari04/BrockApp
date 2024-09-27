package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.database.MemoEntity
import com.example.brockapp.viewmodel.MemoViewModel

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class DailyActivityAdapter(private val list: List<MemoEntity>, private val viewModel: MemoViewModel): RecyclerView.Adapter<DailyActivityViewHolder>() {
    private val selectedMemos = mutableListOf<MemoEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyActivityViewHolder {
        val activityItem = LayoutInflater.from(parent.context).inflate(R.layout.cell_memo, parent, false)
        return DailyActivityViewHolder(activityItem, viewModel)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DailyActivityViewHolder, position: Int) {
        val memo = list[position]

        holder.bind(memo.title, memo.description)

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