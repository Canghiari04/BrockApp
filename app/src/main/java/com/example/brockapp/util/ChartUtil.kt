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
    private var rangeUtil = PeriodRangeImpl()

    inner class CustomChartMarkerView(context: Context, layoutResource: Int): MarkerView(context, layoutResource) {
        private val dates = rangeUtil.datesOfWeek
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
                    " "
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
        val dataSet = BarDataSet(entries, "Bar Chart")
            .apply {
                colors = MutableList(entries.size) { Color.GRAY }
                setDrawValues(false)
            }

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

        val barData = BarData(dataSet)
        barData.setDrawValues(false)
        barChart.data = barData

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

    fun populateLineChart(label: String, lineChart: LineChart, entries: List<Entry>) {
        val lineDataSet = LineDataSet(entries, label)
        lineDataSet.setDrawValues(false)

        // Define the properties of the line
        lineDataSet.lineWidth = 2f
        lineDataSet.color = Color.RED
        lineDataSet.fillColor = Color.RED
        lineDataSet.valueTextColor = Color.BLACK

        // Define the properties of the circles
        lineDataSet.setDrawCircles(true)
        lineDataSet.setCircleColors(Color.GRAY)
        lineDataSet.setDrawFilled(true)

        // Define the data inside the plot
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Define the x axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Define the y axis
        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(false)

        // Disable the superfluous features
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        lineChart.animateY(1000)
        lineChart.invalidate()
    }

    fun populatePieChart(entries: List<PieEntry>, pieChart: PieChart, context: Context) {
        // I passed a mutable list of colors to obtain the possibility to change the color while the app running
        val dataSet = PieDataSet(entries, " ").apply {
            setDrawValues(true)
            valueTextSize = 12f
            valueTypeface = Typeface.DEFAULT_BOLD
            colors = MutableList(entries.size) { Color.GRAY }
        }

        val data = PieData(dataSet)
        pieChart.data = data

        val marker = CustomChartMarkerView(context, R.layout.marker_chart_view)
        pieChart.marker = marker

        pieChart.legend.isEnabled = false
        pieChart.description?.isEnabled = false

        pieChart.invalidate()
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)

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
