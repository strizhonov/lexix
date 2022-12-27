package com.strizhonovapps.lexicaapp.view

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
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.Word
import com.strizhonovapps.lexicaapp.service.LevelColorDefiner
import com.strizhonovapps.lexicaapp.service.WordService
import com.strizhonovapps.lexicaapp.viewsupport.VerticalTextView
import java.text.DecimalFormat
import javax.inject.Inject


class StatsFragment : Fragment() {

    private val minBarsCount = 5

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var levelColorDefiner: LevelColorDefiner

    private var barChart: BarChart? = null
    private var ifEmptyTextView: TextView? = null
    private var xAxisTv: TextView? = null
    private var yAxisTv: VerticalTextView? = null

    private lateinit var mView: View

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment__stats, container, false)
        drawChart(mView)
        return mView
    }

    override fun onResume() {
        super.onResume()
        drawChart(mView)
    }

    private fun drawChart(view: View) {
        barChart = view.findViewById(R.id.fragment_stats__rounded_bar_chart__main_chart)
        ifEmptyTextView = view.findViewById(R.id.fragment_stats__text_view__no_words_message)
        xAxisTv = view.findViewById(R.id.fragment_stats__text_view__x_axis_name)
        yAxisTv = view.findViewById(R.id.fragment_stats__text_view__y_axis_name)

        runBackgroundThenUi(
            requireActivity(),
            { wordService.findAllAvailableForStats() },
            (::setViewFromWords)
        )
    }

    private fun setViewFromWords(words: List<Word>) {
        if (words.isEmpty()) {
            setEmptyVisibility()
        } else {
            setDefaultVisibility()
            val levels = words.map(Word::level)
            initializeBarChart(requireNotNull(barChart), levels)
        }
    }

    private fun setDefaultVisibility() {
        barChart?.visibility = View.VISIBLE
        xAxisTv?.visibility = View.VISIBLE
        yAxisTv?.visibility = View.VISIBLE
        ifEmptyTextView?.visibility = View.GONE
    }

    private fun setEmptyVisibility() {
        barChart?.visibility = View.GONE
        xAxisTv?.visibility = View.GONE
        yAxisTv?.visibility = View.GONE
        ifEmptyTextView?.visibility = View.VISIBLE
    }

    private fun initializeBarChart(barChart: BarChart, levels: List<Int>) {
        configureXAxis(barChart.xAxis)
        configureYAxis(barChart.axisLeft)
        configureBarData(barChart, levels)
        configureBarChart(barChart)
        barChart.invalidate()
    }

    private fun configureBarChart(barChart: BarChart) {
        barChart.setDrawValueAboveBar(false)
        barChart.setDrawGridBackground(true)
        barChart.highlightValues(null)
        barChart.description = Description()
        barChart.description.text = ""
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setScaleEnabled(false)
        barChart.data.dataSets
            .filterIsInstance(BarDataSet::class.java)
            .forEach { it.highLightAlpha = 0 }
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

    private fun configureBarData(barChart: BarChart, levels: List<Int>) {
        val levelsAndCounts = getLevelsAndCounts(levels).sortedBy { it.first }

        val yVals = levelsAndCounts
            .map { levelAndCount ->
                BarEntry(
                    levelAndCount.first.toFloat(),
                    levelAndCount.second.toFloat()
                )
            }

        val barDataSet = BarDataSet(yVals, "")
        barDataSet.colors = levelsAndCounts.map { lvlAndCount ->
            ContextCompat.getColor(
                requireContext(),
                levelColorDefiner.defineColor(lvlAndCount.first)
            )
        }
        val dataSets = listOf(barDataSet)
        val barData = BarData(dataSets)
        barData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                if (value < 1) ""
                else DecimalFormat("###,###,##0").format(value)
        })
        barChart.data = barData
    }

    private fun getLevelsAndCounts(levels: List<Int>): List<Pair<Int, Int>> {
        val levelsAndCounts = levels.groupingBy { it }
            .eachCount()
            .toSortedMap()
            .map { (level, count) -> Pair(level, count) }
            .toMutableList()

        val maxLevel = levels.maxOrNull() ?: 0
        val minLevel = levels.minOrNull() ?: 0

        repeat(minBarsCount - maxLevel + minLevel - 1) { idx ->
            levelsAndCounts.add(Pair(maxLevel + idx + 1, 0))
        }
        return levelsAndCounts
    }

}