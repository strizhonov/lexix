package com.strizhonovapps.lexixapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.dao.allAvailable
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.service.LevelColorDefiner
import com.strizhonovapps.lexixapp.service.WordFreezeTimeDefiner
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.viewsupport.VerticalTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject


@AndroidEntryPoint
class StatsFragment : Fragment() {

    private val minBarsCount = 5

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var levelColorDefiner: LevelColorDefiner

    @Inject
    lateinit var freezeTimeDefiner: WordFreezeTimeDefiner

    private lateinit var statsMainView: View
    private var ifEmptyTextView: TextView? = null

    private var levelsBarChart: BarChart? = null
    private var levelsXAxisTv: TextView? = null
    private var levelsYAxisTv: VerticalTextView? = null

    private var freezePeriodsBarChart: BarChart? = null
    private var freezePeriodsXAxisTv: TextView? = null
    private var freezePeriodsYAxisTv: VerticalTextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        statsMainView = inflater.inflate(R.layout.fragment__stats, container, false)
        return statsMainView
    }

    override fun onResume() {
        super.onResume()
        drawCharts(statsMainView)
    }

    private fun drawCharts(view: View) {
        ifEmptyTextView = view.findViewById(R.id.fragment_stats__text_view__no_words_message)

        levelsBarChart = view.findViewById(R.id.fragment_stats__rounded_bar_chart__levels_chart)
        levelsXAxisTv = view.findViewById(R.id.fragment_stats__text_view__levels_x_axis)
        levelsYAxisTv = view.findViewById(R.id.fragment_stats__text_view__levels_y_axis)

        freezePeriodsBarChart =
            view.findViewById(R.id.fragment_stats__rounded_bar_chart__freeze_chart)
        freezePeriodsXAxisTv = view.findViewById(R.id.fragment_stats__text_view__freeze_x_axis)
        freezePeriodsYAxisTv = view.findViewById(R.id.fragment_stats__text_view__freeze_y_axis)

        CoroutineScope(Dispatchers.IO).launch {
            val words = wordService.findAll(allAvailable)
            CoroutineScope(Dispatchers.Main).launch {
                setStatsView(words)
            }
        }
    }

    private fun setStatsView(words: List<Word>) {
        if (words.isEmpty()) {
            setEmptyVisibility()
            return
        }

        setDefaultVisibility()

        val levels = words.map(Word::level)
        levelsBarChart?.let { barChart ->
            initializeBarChart(barChart) {
                val levelsAndCounts = countEach(levels).sortedBy { it.first }
                val barData = getBarData(levelsAndCounts, levelColorDefiner::defineColor)
                barChart.data = barData
            }
        }

        val freezePeriods = freezeTimeDefiner.getFreezePeriods(words.map { it.getTargetDate() })
        freezePeriodsBarChart?.let { barChart ->
            initializeBarChart(barChart) {
                val freezePeriodsAndCounts = countEach(freezePeriods).sortedBy { it.first }
                val barData = getBarData(freezePeriodsAndCounts) { R.color.accent_alternative }
                freezePeriodsBarChart?.data = barData
            }
        }
    }

    private fun setDefaultVisibility() {
        ifEmptyTextView?.visibility = View.GONE

        freezePeriodsBarChart?.visibility = View.VISIBLE
        freezePeriodsXAxisTv?.visibility = View.VISIBLE
        freezePeriodsYAxisTv?.visibility = View.VISIBLE

        levelsBarChart?.visibility = View.VISIBLE
        levelsXAxisTv?.visibility = View.VISIBLE
        levelsYAxisTv?.visibility = View.VISIBLE
    }

    private fun setEmptyVisibility() {
        ifEmptyTextView?.visibility = View.VISIBLE

        freezePeriodsBarChart?.visibility = View.GONE
        freezePeriodsXAxisTv?.visibility = View.GONE
        freezePeriodsYAxisTv?.visibility = View.GONE

        levelsBarChart?.visibility = View.GONE
        levelsXAxisTv?.visibility = View.GONE
        levelsYAxisTv?.visibility = View.GONE
    }

    private fun initializeBarChart(barChart: BarChart, barDataFn: () -> Unit) {
        configureXAxis(barChart.xAxis)
        configureYAxis(barChart.axisLeft)
        barDataFn()
        configureBarChart(barChart)
        barChart.invalidate()
    }

    private fun configureBarChart(barChart: BarChart) {
        barChart.setDrawValueAboveBar(true)
        barChart.setDrawGridBackground(true)
        barChart.highlightValues(null)
        barChart.description = Description()
        barChart.description.text = ""
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setScaleEnabled(false)
        barChart.data.dataSets
            .filterIsInstance(BarDataSet::class.java)
            .forEach { barDateSet -> barDateSet.highLightAlpha = 0 }
    }

    private fun configureXAxis(xAxis: XAxis) {
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
    }

    private fun configureYAxis(yAxis: YAxis) {
        yAxis.setDrawGridLines(false)
        yAxis.spaceTop = 30f
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
    }

    private fun getBarData(
        listOfXsAndYs: List<Pair<Int, Int>>,
        colorFn: (Int) -> Int
    ): BarData {
        val yVals = listOfXsAndYs
            .map { xAndY ->
                BarEntry(
                    xAndY.first.toFloat(),
                    xAndY.second.toFloat()
                )
            }

        val barDataSet = BarDataSet(yVals, "")
        barDataSet.colors = listOfXsAndYs.map { xAndY ->
            ContextCompat.getColor(
                requireContext(),
                colorFn(xAndY.first)
            )
        }
        val dataSets = listOf(barDataSet)
        val barData = BarData(dataSets)
        barData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                if (value < 1) ""
                else DecimalFormat("###,###,##0").format(value)
        })
        return barData
    }

    private fun countEach(values: List<Int>): List<Pair<Int, Int>> {
        val itemAndItsCount = values.groupingBy { it }
            .eachCount()
            .toSortedMap()
            .map { (level, count) -> Pair(level, count) }
            .toMutableList()

        val max = values.maxOrNull() ?: 0
        val min = values.minOrNull() ?: 0

        repeat(minBarsCount - max + min - 1) { idx ->
            itemAndItsCount.add(Pair(max + idx + 1, 0))
        }
        return itemAndItsCount
    }
}