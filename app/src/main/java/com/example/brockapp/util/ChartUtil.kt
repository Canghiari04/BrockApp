package com.example.brockapp.util

import com.example.brockapp.R
import com.example.brockapp.interfaces.PeriodRangeImpl

import android.view.View
import android.graphics.Color
import android.content.Context
import android.widget.TextView
import android.graphics.Typeface
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class ChartUtil {
    inner class CustomChartMarkerView(context: Context, layoutResource: Int): MarkerView(context, layoutResource) {
        private val dates = PeriodRangeImpl().datesOfWeek
        private val labelTextView: TextView = findViewById(R.id.marker_value)

        override fun refreshContent(entry: Entry?, highlight: Highlight?) {
            val label: String = when (entry) {
                is BarEntry -> {
                    dates.getValue(entry.x.toInt())
                }

                is PieEntry -> {
                    "${entry.label} activities"
                }

                else -> {
                    when (entry?.data) {
                        "km" -> {
                            "%.3f km".format(entry.y)
                        }

                        "steps" -> {
                            "%.0f steps".format(entry.y)
                        }

                        else -> {
                            " "
                        }
                    }
                }
            }

            labelTextView.text = label

            super.refreshContent(entry, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF(-width / 2f, -height.toFloat())
        }
    }

    fun populateBarChart(barChart: BarChart, entries: List<BarEntry>, context: Context) {
        val dataSet = BarDataSet(entries, "Time spent")

        dataSet.colors = MutableList(entries.size) { Color.GRAY }

        dataSet.setDrawValues(false)

        // Define the dataset to insert inside the chart
        val barData = BarData(dataSet)
        barData.setDrawValues(false)
        barChart.data = barData

        // Marker used to see the data on the chart
        val marker = CustomChartMarkerView(context, R.layout.marker_chart_view)
        barChart.marker = marker

        // Removing the background grid lines
        barChart.setDrawGridBackground(false)
        barChart.setBackgroundColor(Color.WHITE)

        // Removing the grid lines and positioning the x axis to the bottom of the plot
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Removing other grid lines
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.axisRight.setDrawGridLines(false)

        barChart.xAxis.isGranularityEnabled = true
        barChart.xAxis.granularity = 1f

        // Define the start of the bar plot
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.axisMinimum = 0f

        barChart.setExtraOffsets(0f, 0f, 0f, 20f)
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.legend.isEnabled = false
        barChart.invalidate()

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(entry: Entry?, p1: Highlight?) {
                if (entry is BarEntry) {
                    val selectedEntry = entries.indexOf(entry)

                    for (i in entries.indices) {
                        dataSet.colors[i] = if (i == selectedEntry) Color.RED else Color.GRAY
                    }

                    barChart.invalidate()
                }
            }

            override fun onNothingSelected() { }
        })
    }

    fun populateLineChart(label: String,  entries: List<Entry>, lineChart: LineChart, context: Context) {
        when {
            label.contains("Distance") -> {
                entries.forEach {
                    it.data = "km"
                }
            }

            label.contains("Kilometers") -> {
                entries.forEach {
                    it.data = "km"
                }
            }

            label.contains("Steps") -> {
                entries.forEach {
                    it.data = "steps"
                }
            }
        }

        val dataSet = LineDataSet(entries, label)

        dataSet.lineWidth = 2f
        dataSet.color = Color.RED
        dataSet.fillColor = Color.RED
        dataSet.setCircleColors(Color.GRAY)
        dataSet.valueTextColor = Color.BLACK

        dataSet.setDrawFilled(true)
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(true)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val marker = CustomChartMarkerView(context, R.layout.marker_chart_view)
        lineChart.marker = marker

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(false)

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        lineChart.animateX(1000)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.invalidate()
    }

    fun populatePieChart(entries: List<PieEntry>, pieChart: PieChart, context: Context) {
        // I passed a mutable list of colors to obtain the possibility to change the color while the app running
        val dataSet = PieDataSet(entries, " ")

        dataSet.valueTextSize = 12f
        dataSet.valueTypeface = Typeface.DEFAULT_BOLD
        dataSet.colors = MutableList(entries.size) { Color.GRAY }

        dataSet.setDrawValues(true)

        val data = PieData(dataSet)
        pieChart.data = data

        val marker = CustomChartMarkerView(context, R.layout.marker_chart_view)
        pieChart.marker = marker

        pieChart.legend.isEnabled = false
        pieChart.description?.isEnabled = false

        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()

        pieChart.visibility = View.VISIBLE

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(entry: Entry?, p1: Highlight?) {
                if (entry is PieEntry) {
                    val selectedEntry = entries.indexOf(entry)

                    for (i in entries.indices) {
                        dataSet.colors[i] = if (i == selectedEntry) Color.RED else Color.GRAY
                    }

                    pieChart.invalidate()
                }
            }

            override fun onNothingSelected() {
                for (i in entries.indices) {
                    dataSet.colors[i] = Color.GRAY
                }

                pieChart.invalidate()
            }
        })
    }
}
