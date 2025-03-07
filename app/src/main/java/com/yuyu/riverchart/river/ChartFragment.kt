package com.yuyu.riverchart.river

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.yuyu.riverchart.Application
import com.yuyu.riverchart.R
import com.yuyu.riverchart.data.RiverChart
import com.yuyu.riverchart.data.TimesValue
import com.yuyu.riverchart.databinding.FragmentChartBinding
import com.yuyu.riverchart.factory.ViewModelFactory


class ChartFragment : Fragment() {

    private var defautHighlight :Highlight? = null

    private lateinit var binding: FragmentChartBinding
    private val viewModel by viewModels<ChartViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChartBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val lineChart = binding.lineChart
        lineChart.run {
            setBackgroundColor(Color.BLACK)
            setPinchZoom(true)
            setScaleEnabled(false)
            setDrawGridBackground(false)
            setMaxVisibleValueCount(2)
            legend.isEnabled = false
            description.isEnabled = false
            isSelected = true
        }

        lineChart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val x = lineChart.data.getDataSetForEntry(e).getEntryIndex(e)
                viewModel.getSelectData(x)

                h?.let {
                    defautHighlight = h
                }
            }

            override fun onNothingSelected() {
                lineChart.highlightValue(defautHighlight)
            }
        })

        viewModel.riverData.observe(viewLifecycleOwner) {
            initDataSet(lineChart, it)
            initX(lineChart, it.yearMonth)
            initY(lineChart)
            viewModel.getSelectData(it.yearMonth.size - 1)
        }

        viewModel.timesValue0.observe(viewLifecycleOwner) {
            toTimesStr(binding.color0Tv, it)
        }

        viewModel.timesValue1.observe(viewLifecycleOwner) {
            toTimesStr(binding.color1Tv, it)
        }

        viewModel.timesValue2.observe(viewLifecycleOwner) {
            toTimesStr(binding.color2Tv, it)
        }

        viewModel.timesValue3.observe(viewLifecycleOwner) {
            toTimesStr(binding.color3Tv, it)
        }

        viewModel.timesValue4.observe(viewLifecycleOwner) {
            toTimesStr(binding.color4Tv, it)
        }

        viewModel.timesValue5.observe(viewLifecycleOwner) {
            toTimesStr(binding.color5Tv, it)
        }

        return binding.root
    }

    fun initDataSet(lineChart: LineChart, list: RiverChart) {

        val value0 = ArrayList<Entry>()
        val value1 = ArrayList<Entry>()
        val value2 = ArrayList<Entry>()
        val value3 = ArrayList<Entry>()
        val value4 = ArrayList<Entry>()
        val value5 = ArrayList<Entry>()
        val monthPrice = ArrayList<Entry>()

        list.yearMonth.forEachIndexed { index, s ->
            value0.add(Entry(index.toFloat(), list.basePE0[index]))
            value1.add(Entry(index.toFloat(), list.basePE1[index]))
            value2.add(Entry(index.toFloat(), list.basePE2[index]))
            value3.add(Entry(index.toFloat(), list.basePE3[index]))
            value4.add(Entry(index.toFloat(), list.basePE4[index]))
            value5.add(Entry(index.toFloat(), list.basePE5[index]))
            monthPrice.add(Entry(index.toFloat(), list.monthPrice[index]))
        }

        val data6 = fillLineDataSet(value0, R.color.line_color5, R.drawable.line_area_color5, null)
        val data5 = fillLineDataSet(value1, R.color.line_color4, R.drawable.line_area_color4, data6)
        val data4 = fillLineDataSet(value2, R.color.line_color3, R.drawable.line_area_color3, data5)
        val data3 = fillLineDataSet(value3, R.color.line_color2, R.drawable.line_area_color2, data4)
        val data2 = fillLineDataSet(value4, R.color.line_color1, R.drawable.line_area_color1, data3)
        val data1 = fillLineDataSet(value5, R.color.line_color0, R.drawable.line_area_color0, data2)

        val data = LineData(
            data1,
            data2,
            data3,
            data4,
            data5,
            data6,
            lineDataSet(monthPrice, R.color.red, DEFAULT_WIDTH)
        )

        lineChart.renderer = MyLineLegendRenderer(lineChart, lineChart.animator, lineChart.viewPortHandler)
        lineChart.data = data
        lineChart.highlightValue((list.yearMonth.size - 1).toFloat(), 1)
        lineChart.invalidate()
    }

    private fun fillLineDataSet(value: ArrayList<Entry>, color: Int, draw: Int, prevDataSet: LineDataSet?): LineDataSet {
        val set = LineDataSet(value, "")
        val drawable = ContextCompat.getDrawable(requireContext(), draw)

        set.run {
            mode = LineDataSet.Mode.LINEAR
            lineWidth = DEFAULT_WIDTH
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = drawable
            set.color = resources.getColor(color, null)
            valueTextColor = resources.getColor(color, null)
            fillFormatter = MyFillFormatter(prevDataSet)

            if (prevDataSet == null) {
                setDrawFilled(false)
                lineWidth = DIFF_WIDTH
            }

            initHighlight(set)
        }

        return set
    }

    private fun lineDataSet(value: ArrayList<Entry>, color: Int, width: Float): LineDataSet {
        val set = LineDataSet(value, "")

        set.run {
            mode = LineDataSet.Mode.LINEAR
            lineWidth = width
            setDrawCircles(false)
            setDrawValues(false)
            set.color = resources.getColor(color, null)

            initHighlight(set)
        }

        return set
    }

    private fun initX(lineChart: LineChart, date: List<String>) {
        val xAxis: XAxis = lineChart.xAxis

        xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            gridColor = resources.getColor(R.color.grid, null)
            textSize = TEXTSIZE
            setLabelCount(6,false)
            setCenterAxisLabels(true)
            setDrawGridLines(true)
        }

        val xList: MutableList<String> = ArrayList()
        for (i in date.indices) {
            xList.add(StringBuilder(date[i]).insert(4,'/').toString())
        }
        xAxis.valueFormatter = IndexAxisValueFormatter(xList)
    }

    private fun initY(lineChart: LineChart) {
        lineChart.axisLeft.isEnabled = false

        val yAxisR = lineChart.axisRight
        yAxisR.run {
            isEnabled = true
            textSize = TEXTSIZE
            labelCount = 6
            textColor = Color.WHITE
            gridColor = resources.getColor(R.color.grid, null)
            setDrawGridLines(true)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        }
    }

    private fun initHighlight(set: LineDataSet) {
        set.run {
            highLightColor = Color.WHITE
            highlightLineWidth = resources.getDimensionPixelOffset(R.dimen.highlight_width).toFloat()
            setDrawHorizontalHighlightIndicator(false)
        }
    }

    private fun toTimesStr(textView: TextView, data: TimesValue) {
        textView.text = "${data.basePE}倍 ${data.basePricePE}"
    }

    companion object {
        val DEFAULT_WIDTH = 1.5f
        val TEXTSIZE = 12f
        val DIFF_WIDTH = 10F
    }
}