package com.example.brockapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brockapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate

class ChartsActivity : AppCompatActivity() {


    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.charts_activity)

        // Inizializza i grafici
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)

        // Popola l'istogramma
        setupBarChart()

        // Popola il diagramma a torta
        setupPieChart()
    }

    private fun setupBarChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 4f))
        entries.add(BarEntry(1f, 8f))
        entries.add(BarEntry(2f, 6f))
        entries.add(BarEntry(3f, 2f))
        entries.add(BarEntry(4f, 10f))

        val dataSet = BarDataSet(entries, "Dati")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val data = BarData(dataSet)
        barChart.data = data
        barChart.description.text = "Numero di passi"
        barChart.animateY(1000)
    }

    private fun setupPieChart() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(25f, "Categoria A"))
        entries.add(PieEntry(35f, "Categoria B"))
        entries.add(PieEntry(40f, "Categoria C"))

        val dataSet = PieDataSet(entries, "Dati")
        dataSet.colors = ColorTemplate.PASTEL_COLORS.toList()

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.text = "Diagramma a torta di esempio"
        pieChart.animateY(1000)
    }
}