package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.activity.RegistrationMemoActivity

import android.view.View
import android.widget.Button
import android.content.Intent
import android.view.ViewGroup
import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

class DailyMemoAdapter(private val date: String?, private val list: List<MemosEntity>, private val context: Context): RecyclerView.Adapter<DailyMemoAdapter.DailyMemoViewHolder>() {

    private val selectedMemos = mutableListOf<MemosEntity>()

    inner class DailyMemoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_memo)
        val button: Button = itemView.findViewById(R.id.button_modify_memo)
        val title: TextView = itemView.findViewById(R.id.text_view_memo_title)
        val activityType: TextView = itemView.findViewById(R.id.text_view_type_activity)
        val description: TextView = itemView.findViewById(R.id.text_view_memo_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DailyMemoViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.cell_memo, parent, false)
        return DailyMemoViewHolder(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DailyMemoViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.title
        holder.description.text = item.description
        holder.activityType.text = item.activityType

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedMemos.add(item) else selectedMemos.remove(item)
        }

        holder.button.setOnClickListener {
            val title = item.title
            val description = item.description

            val intent = Intent(context, RegistrationMemoActivity::class.java).apply {
                putExtra("CALENDAR_DATE", date)
                putExtra("ID_MEMO", item.id)
                putExtra("TITLE_MEMO", title)
                putExtra("DESCRIPTION_MEMO", description)
            }

            context.startActivity(intent)
        }
    }

    fun getMemosSelected(): List<MemosEntity> {
        return selectedMemos
    }
}