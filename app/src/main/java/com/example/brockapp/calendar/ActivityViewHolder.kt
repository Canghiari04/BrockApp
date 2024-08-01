package com.example.brockapp.calendar

import com.example.brockapp.R

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val activity_text_view = itemView.findViewById<TextView>(R.id.activity_day_text)
    private val activity_image_view = itemView.findViewById<ImageView>(R.id.activity_cell_image)

    fun bindActivity(nameActivity: String) {
        activity_text_view.text = nameActivity

        when(nameActivity) {
            "Walk" -> {
                activity_image_view.setImageResource(R.drawable.baseline_directions_walk_24)
            }
            "Vehicle" -> {
                activity_image_view.setImageResource(R.drawable.baseline_directions_car_24)
            }
            "Still" -> {
                activity_image_view.setImageResource(R.drawable.baseline_chair_24)
            }
            else -> activity_image_view.setImageResource(R.drawable.ic_launcher_background)
        }
    }
}