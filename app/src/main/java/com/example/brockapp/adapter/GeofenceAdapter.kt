package com.example.brockapp.adapter

import com.example.brockapp.R
import com.example.brockapp.data.GeofenceTransition

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.core.text.HtmlCompat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape

class GeofenceAdapter(private val list: List<GeofenceTransition>): RecyclerView.Adapter<GeofenceAdapter.GeofenceViewHolder>() {

    inner class GeofenceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.text_view_title_geofence)
        val timeSpent: TextView = itemView.findViewById(R.id.text_view_spent_time)
        val accessCount: TextView = itemView.findViewById(R.id.text_view_access_count)
        val composeView: ComposeView = itemView.findViewById(R.id.compose_view_cell_geofence)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ): GeofenceViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.cell_geofence, parent, false)
        return GeofenceViewHolder(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: GeofenceViewHolder, position: Int) {
        val item = list[position]
        
        holder.title.text = item.nameLocation
        holder.timeSpent.text = HtmlCompat.fromHtml(
            "<b>Average time spent:</b> ${item.averageTime}",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        holder.accessCount.text = HtmlCompat.fromHtml(
            "<b>Number of accesses:</b> ${item.count}",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        holder.composeView.setContent {
            CardViewTimeStamp(item.timestamps)
        }
    }

    @Composable
    fun CardViewTimeStamp(items: List<Pair<String, String>>) {
        var expanded by remember { mutableStateOf(false) }
        val icon = if (!expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp
        
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.grey)
            ),
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time stamps",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        icon,
                        contentDescription = "Dropdown menu"
                    )
                }

                if (expanded) {
                    items.forEach { item ->
                        DateTimeDisplay(
                            date = item.first,
                            time = item.second
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DateTimeDisplay(date: String, time: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp, 16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar"
            )
            Column(
                modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}