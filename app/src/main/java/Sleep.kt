import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.morales.bnatest.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.annotation.NonNull
import com.github.mikephil.charting.components.YAxis


class Sleep : Fragment() {

    //定义了就寝时间和日期
    //之后将日期传入这四个变量即可


    private val bedtimeData = arrayOf("00:00", "00:30", "23:00", "23:10", "22:30")
    private val wakeupDate = arrayOf("7:50", "8:30", "8:15", "7:40", "9:00")
    private val dates = arrayOf("3-21", "3-22", "3-23", "3-24", "3-25")
    private val snoringData = floatArrayOf(3f, 2.5f, 1f, 1.3f, 0f)  //每天的打鼾时长

    //定义了lineChart和textView
    private lateinit var lineChart: LineChart
    private lateinit var textView: android.widget.TextView

    //起床时间部分
    private lateinit var wakeupLineChart: LineChart
    private lateinit var wakeupTextView: android.widget.TextView

    //睡眠时长部分
    private lateinit var sleepDurationLineChart: BarChart
    private lateinit var sleepDurationTextView: android.widget.TextView

    //打鼾时长部分
    private lateinit var snoringLineChart: LineChart
    private lateinit var snoringTextView: android.widget.TextView



    //注：onCreateView 方法是 Fragment 生命周期中的一个重要方法，用于创建和返回 Fragment 的视图。
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleep_thrend, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 通过 R.id 查找视图，并明确指定类型
        lineChart = view.findViewById<LineChart>(R.id.linechart1)
        textView = view.findViewById<android.widget.TextView>(R.id.textView1)

        wakeupLineChart = view.findViewById<LineChart>(R.id.linechart2)
        wakeupTextView = view.findViewById<android.widget.TextView>(R.id.textView2)

        sleepDurationLineChart = view.findViewById<BarChart>(R.id.linechart3)
        sleepDurationTextView = view.findViewById<android.widget.TextView>(R.id.textView3)

        snoringLineChart = view.findViewById<LineChart>(R.id.linechart4)
        snoringTextView = view.findViewById<android.widget.TextView>(R.id.textView4)


        // 计算平均就寝时间并更新视图
        val averageBedtime = calculateAverageBedtime()
        updateAverageBedtimeTextView(averageBedtime)

        //计算平均起床时间并更新
        val averageWakeupTime = calculateAverageTime(wakeupDate)
        updateWakeUpTextView(wakeupTextView, averageWakeupTime)

        //计算平均睡眠时间并更新
        val averageSleepDuration = calculateAverageSleepDuration(bedtimeData, wakeupDate)
        updateSleepDurationTextView(sleepDurationTextView, averageSleepDuration)

        //计算平均打鼾时间并更新
        val averageSnoringTime = calculateAverageSnoringTime(snoringData)
        updateSnoringTextView(snoringTextView, averageSnoringTime)

        // 配置折线图
        setupLineChart(lineChart)//就寝时间
        setupWakeupLineChart(wakeupLineChart, wakeupDate)//起床时间
        setupSleepDurationLineChart(sleepDurationLineChart, bedtimeData, wakeupDate)//睡觉时长
        setupSnoringLineChart(snoringLineChart,snoringData)//打鼾时长
    }

    /**
     * function:通过遍历计算平均就寝时间
     * @param：无
     * @return：averageBedtime平均就寝时间
     * */
    private fun calculateAverageBedtime(): Int {
        var totalMinutes = 0
        for (time in bedtimeData) {
            val parts = time.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            //totalMinutes += hours * 60 + minutes
            totalMinutes += convertToAdjustedMinutes(hours, minutes)
        }
        return totalMinutes / bedtimeData.size
    }

    /**
     * functiom:计算平均时间
     * @param：待计算的数组
     * @return：平均时间
     * */
    private fun calculateAverageTime(timeData: Array<String>): Int {
        var totalMinutes = 0
        for (time in timeData) {
            val parts = time.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            totalMinutes += hours * 60 + minutes
        }
        return totalMinutes / timeData.size
    }

    private fun updateWakeUpTextView(textView: android.widget.TextView, averageTime: Int) {
        val hours = averageTime / 60
        val minutes = averageTime % 60
        val formattedTime = String.format("%02d:%02d", hours, minutes)
        textView.text = "近五天的平均时间为：$formattedTime"
    }

    /**
     * function:将时间转换为调整后的分钟数，以18点为例子
     * @param:小时
     * @param分钟
     * @return：调整后的分钟
     * */

    private fun convertToAdjustedMinutes(hours: Int, minutes: Int): Int {
        var adjustedHours = hours
        if (adjustedHours < 18) {
            adjustedHours += 24
        }
        return (adjustedHours - 18) * 60 + minutes
    }


    /**
     * function:更新textView
     * @param:平均就寝时间
     * @return：无
     * */

    private fun updateAverageBedtimeTextView(averageBedtime: Int) {
        /*
        val hours = averageBedtime / 60
        val minutes = averageBedtime % 60
        val formattedTime = String.format("%02d:%02d", hours, minutes)
        textView.text = "近5天内，你的平均的就寝时间是：$formattedTime"*/

        val adjustedHours = averageBedtime / 60 + 18
        val finalHours = if (adjustedHours >= 24) adjustedHours - 24 else adjustedHours
        val minutes = averageBedtime % 60
        val formattedTime = String.format("%02d:%02d", finalHours, minutes)
        textView.text = "近 5 天内，你的平均的就寝时间是：$formattedTime"

    }

    /**
     * function:将时间格式转换为分钟
     * @param：时间格式
     * @return:分钟
     * */
    private fun convertToMinutes(time: String): Int {
        val parts = time.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        return hours * 60 + minutes
    }

    /**
     * function:计算平均睡眠时间
     * @param：bedtimeDate睡眠时间
     * @param：wakeupDate起床时间
     * */
    private fun calculateAverageSleepDuration(bedtimeData: Array<String>, wakeupData: Array<String>): Float {
        var totalDuration = 0f
        for (i in bedtimeData.indices) {
            val bedtime = convertToMinutes(bedtimeData[i])
            val wakeup = convertToMinutes(wakeupData[i])
            var duration = wakeup - bedtime
            if (duration < 0) {
                duration += 24 * 60
            }
            totalDuration += duration
        }
        return totalDuration / bedtimeData.size / 60f
    }

    /**
     * function:更新平均睡眠时间文本
     * */
    private fun updateSleepDurationTextView(textView: android.widget.TextView, averageSleepDuration: Float) {
        textView.text = String.format("近五天的平均睡眠时长为%.1f小时", averageSleepDuration)
    }

    /**
     * function:计算平均打鼾时长
     * @param：五日打鼾时长array
     * @return：平均打鼾时长
     * */
    private fun calculateAverageSnoringTime(snoringData: FloatArray): Float {
        var total = 0f
        for (time in snoringData) {
            total += time
        }
        return total / snoringData.size
    }

    /**
     * function:更新打呼噜文本
     * */
    private fun updateSnoringTextView(textView: android.widget.TextView, averageSnoringTime: Float) {
        textView.text = String.format("近五天的平均打鼾时间为%.1fh", averageSnoringTime)
    }

    /**
     * function:画出图表
     * */
    /**
     * function: 画出图表
     * */
    private fun setupLineChart(chart: LineChart) {
        val entries = ArrayList<Entry>()
        for (i in bedtimeData.indices) {
            //Log.d("SleepTreadChart","i为${i}")
            val parts = bedtimeData[i].split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            val totalMinutes = convertToAdjustedMinutes(hours, minutes)
            entries.add(Entry(i.toFloat(), totalMinutes.toFloat()))
        }


        // 数据集加载
        // 创建一个 LineDataSet 对象，用于存储折线图的数据点
        val dataSet = LineDataSet(entries, "就寝时间")// "就寝时间" 是数据集的标签，显示在图例中
        //Log.d("SleepTreadChart","dataset为${dataSet}")
        dataSet.color = android.graphics.Color.WHITE           // 设置折线的颜色为白色
        dataSet.valueTextColor = android.graphics.Color.WHITE   // 设置数据点文本的颜色为白色

        dataSet.setDrawCircles(true)// 设置是否绘制数据点的圆圈
        dataSet.circleRadius=5f

        dataSet.setDrawValues(true)// 设置是否在数据点上显示值
        dataSet.valueFormatter = object : ValueFormatter() {   // 自定义 Y 轴的值格式化器
            override fun getFormattedValue(value: Float): String {
                val adjustedHours = value.toInt() / 60 + 18
                val finalHours = if (adjustedHours >= 24) adjustedHours - 24 else adjustedHours
                val minutes = value.toInt() % 60
                return String.format("%02d:%02d", finalHours, minutes)  // 格式化为 "HH:MM" 的时间格式
            }
        }
        dataSet.highLightColor = android.graphics.Color.YELLOW // 突出选中的值的颜色
        dataSet.setDrawHighlightIndicators(true)

        // 创建 LineData 对象，将数据集添加到折线图中
        val lineData = LineData(dataSet)// 将 LineData 对象设置到折线图控件中

        chart.data = lineData

        // 设置 X 轴
        val xAxis = chart.xAxis
        xAxis.setLabelCount(4)
        xAxis.position = XAxis.XAxisPosition.BOTTOM    // 将 X 轴的位置设置在图表底部
        xAxis.valueFormatter  = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                // 根据 value 的范围映射到 dates 数组的索引
                return dates[value.toInt()]
            }
        }
        xAxis.textColor = android.graphics.Color.WHITE




        // 设置 Y 轴
        val leftAxis = chart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {   // 自定义 Y 轴的值格式化器
            override fun getFormattedValue(value: Float): String {
                Log.d("SleepTreadChart","valueX为${value}")
                val adjustedHours = value.toInt() / 60 + 18
                val finalHours = if (adjustedHours >= 24) adjustedHours - 24 else adjustedHours
                val minutes = value.toInt() % 60
                return String.format("%02d:%02d", finalHours, minutes)  // 格式化为 "HH:MM" 的时间格式
            }
        }



        leftAxis.textColor = android.graphics.Color.WHITE // 设置左侧 Y 轴文本颜色为白色
        leftAxis.axisMinimum = 0f // 设置 Y 轴最小值为 0
        leftAxis.axisMaximum = (4 * 60 + 6 * 60).toFloat() // 设置 Y 轴最大值为 4 点对应的分钟数（考虑从 18 点开始）
        leftAxis.setDrawGridLines(true)

        chart.axisRight.isEnabled = false // 禁用右侧 Y 轴
        chart.description.isEnabled = false // 禁用描述
        chart.legend.isEnabled = false // 禁用图例
        //设置网格线
        xAxis.setDrawAxisLine(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.gridLineWidth=1f

        //设置网格线的颜色
        val gridLineColor = ContextCompat.getColor(requireContext(),R.color.mpa_line)
        leftAxis.gridColor = gridLineColor
        chart.xAxis.setDrawGridLines(false)

        chart.setScaleEnabled(false)

        chart.invalidate() // 刷新图表


    }

    /**
     * function:用来画平均起床时间
     * @param：待画的图表
     * @param：待画的日期
     * */


    private fun setupWakeupLineChart(chart: LineChart, timeData: Array<String>) {
        val entries = ArrayList<Entry>()
        for (i in timeData.indices) {
            val parts = timeData[i].split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            val totalMinutes = hours * 60 + minutes
            entries.add(Entry(i.toFloat(), totalMinutes.toFloat()))
        }

        val dataSet = LineDataSet(entries, "起床时间")
        dataSet.color = android.graphics.Color.WHITE
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.setDrawCircles(true)
        dataSet.circleRadius=5f
        dataSet.setDrawValues(true)
        dataSet.valueFormatter = object : ValueFormatter() {   // 自定义 Y 轴的值格式化器
            override fun getFormattedValue(value: Float): String {
                val adjustedHours = value.toInt() / 60
                val finalHours = if (adjustedHours >= 24) adjustedHours - 24 else adjustedHours
                val minutes = value.toInt() % 60
                return String.format("%02d:%02d", finalHours, minutes)  // 格式化为 "HH:MM" 的时间格式
            }
        }
        dataSet.highLightColor = android.graphics.Color.YELLOW
        dataSet.setDrawHighlightIndicators(true)

        val lineData = LineData(dataSet)
        chart.data = lineData

        // 设置 X 轴
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(4);
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return dates[value.toInt()]
            }
        }
        xAxis.textColor = android.graphics.Color.WHITE

        // 设置 Y 轴
        val leftAxis = chart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val hours = value.toInt() / 60
                val minutes = value.toInt() % 60
                return String.format("%02d:%02d", hours, minutes)
            }
        }
        leftAxis.textColor = android.graphics.Color.WHITE
        leftAxis.axisMinimum = (5 * 60).toFloat() // 最低点为 5:00
        leftAxis.axisMaximum = (10 * 60).toFloat() // 最高点为 10:00

        //隐藏XY轴
        xAxis.setDrawAxisLine(false)
        leftAxis.setDrawAxisLine(false)
        //设置网格线
        leftAxis.setDrawGridLines(true)
        leftAxis.gridLineWidth = 1f
        //设置颜色
        val gridLineColor = ContextCompat.getColor(requireContext(), R.color.mpa_line)
        leftAxis.gridColor = gridLineColor

        // 禁用 X 轴网格线
        xAxis.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.setScaleEnabled(false)
        chart.invalidate()
    }

/*
    private fun setupSleepDurationLineChart(chart: LineChart, bedtimeData: Array<String>, wakeupData: Array<String>) {
        val entries = ArrayList<Entry>()
        for (i in bedtimeData.indices) {
            val bedtime = convertToMinutes(bedtimeData[i])
            val wakeup = convertToMinutes(wakeupData[i])
            var duration = wakeup - bedtime
            if (duration < 0) {
                duration += 24 * 60
            }
            entries.add(Entry(i.toFloat(), duration / 60f))
        }

        val dataSet = LineDataSet(entries, "睡眠时长")
        dataSet.color = android.graphics.Color.WHITE
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.setDrawCircles(true)
        dataSet.setDrawValues(true)
        dataSet.highLightColor = android.graphics.Color.YELLOW
        dataSet.setDrawHighlightIndicators(true)

        val lineData = LineData(dataSet)
        chart.data = lineData

        // 设置 X 轴
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return dates[value.toInt()]
            }
        }
        xAxis.textColor = android.graphics.Color.WHITE

        // 设置 Y 轴
        val leftAxis = chart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.1f小时", value)
            }
        }
        leftAxis.textColor = android.graphics.Color.WHITE
        leftAxis.axisMinimum = 4f // 最低点为 4.0 小时
        leftAxis.axisMaximum = 12f // 最高点为 12 小时

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }*/

    //绘制条形图
    private fun setupSleepDurationLineChart(chart: BarChart, bedtimeData: Array<String>, wakeupData: Array<String>) {
        val entries = ArrayList<BarEntry>()
        for (i in bedtimeData.indices) {
            val bedtime = convertToMinutes(bedtimeData[i])
            val wakeup = convertToMinutes(wakeupData[i])
            var duration = wakeup - bedtime
            if (duration < 0) {
                duration += 24 * 60
            }
            entries.add(BarEntry(i.toFloat(), duration / 60f))
        }

        val dataSet = BarDataSet(entries, "睡眠时长")
        //dataSet.color = Color.WHITE
        //设置颜色
        val barColor = ContextCompat.getColor(requireContext(), R.color.mpa_bar_2)
        dataSet.color = barColor

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f
        dataSet.setDrawValues(true)
        dataSet.highLightColor = Color.YELLOW


        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        chart.data = barData

        //设置X轴
        val xAxis = chart.xAxis
        xAxis.position =  XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(5);
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return dates[value.toInt()]
            }
        }
        xAxis.textColor = Color.WHITE

        //设置Y轴
        val leftAxis = chart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.1f", value)
            }
        }
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMinimum = 4f // 最低点为 4.0 小时
        leftAxis.axisMaximum = 12f // 最高点为 12 小时

        // 禁用缩放功能
        chart.setScaleEnabled(false)

        //禁用其坐标轴
        xAxis.setDrawAxisLine(false)
        leftAxis.setDrawAxisLine(false)
        //网格设置
        xAxis.setDrawGridLines(false)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridLineWidth=1f

        addLimitLine(leftAxis,6f,Color.RED)
        addLimitLine(leftAxis,8f,Color.YELLOW)
        addLimitLine(leftAxis,10f,Color.GREEN)

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()

    }


    private fun setupSnoringLineChart(chart: LineChart, snoringData: FloatArray) {
        // 存储打鼾时间数据点的列表
        val entries = ArrayList<Entry>()
        // 遍历打鼾时间数据数组
        for (i in snoringData.indices) {
            // 将每个数据点添加到列表中，X 轴为天数索引，Y 轴为打鼾时间
            entries.add(Entry(i.toFloat(), snoringData[i]))
        }

        // 创建一个 LineDataSet 对象，用于存储折线图的数据点
        val dataSet = LineDataSet(entries, "打鼾时间")
        // 设置折线的颜色
        dataSet.color = android.graphics.Color.CYAN
        // 设置数据点文本的颜色
        dataSet.valueTextColor = android.graphics.Color.WHITE
        // 设置是否绘制数据点的圆圈
        dataSet.setDrawCircles(true)
        dataSet.circleRadius=5f
        // 设置是否在数据点上显示值
        dataSet.setDrawValues(true)
        dataSet.valueTextSize=10f
        // 突出选中的值的颜色
        dataSet.highLightColor = android.graphics.Color.YELLOW
        // 设置是否绘制高亮指示器
        dataSet.setDrawHighlightIndicators(true)
        // 设置线条宽度
        dataSet.lineWidth = 2f
        /*
        // 设置填充颜色
        dataSet.setCircleColor(android.graphics.Color.CYAN)
        // 设置填充透明度
        dataSet.setDrawFilled(true)
        dataSet.fillColor = android.graphics.Color.argb(128, 0, 255, 255)*/

        val areas = listOf(
            AreaInfo(0f, 1f, ContextCompat.getColor(requireContext(), R.color.mpa_snoring_mild)),
            AreaInfo(1f, 3f, ContextCompat.getColor(requireContext(), R.color.mpa_snoring_moderate)),
            AreaInfo(3f, 7f, ContextCompat.getColor(requireContext(), R.color.mpa_snoring_severe))
        )
        addMultipleVerticalLimitLinesAndFillAreas(chart, areas)
        // 创建 LineData 对象，将数据集添加到折线图中
        val lineData = LineData(dataSet)
        // 将 LineData 对象设置到折线图控件中
        chart.data = lineData

        // 设置 X 轴
        val xAxis = chart.xAxis
        // 将 X 轴的位置设置在图表底部
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.setLabelCount(4);

        // 自定义 X 轴的值格式化器
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                // 根据 X 轴的值（索引）从 dates 数组中获取对应的日期字符串
                return dates[value.toInt()]
            }
        }
        // 设置 X 轴文本颜色
        xAxis.textColor = android.graphics.Color.WHITE
        // 设置 X 轴的网格线颜色
        xAxis.gridColor = android.graphics.Color.WHITE
        // 设置 X 轴的网格线宽度
        xAxis.gridLineWidth = 0.5f

        // 设置 Y 轴
        val leftAxis = chart.axisLeft
        // 自定义 Y 轴的值格式化器
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                // 将 Y 轴的值格式化为小时字符串
                return String.format("%.1f", value)
            }
        }
        // 设置左侧 Y 轴文本颜色
        leftAxis.textColor = android.graphics.Color.WHITE

        // 设置 Y 轴的网格线宽度
        leftAxis.gridLineWidth = 0.5f
        // 设置 Y 轴最小值
        leftAxis.axisMinimum = 0f
        // 设置 Y 轴最大值
        leftAxis.axisMaximum = 7f

        // 禁用右侧 Y 轴
        chart.axisRight.isEnabled = false
        // 禁用描述
        chart.description.isEnabled = false
        // 禁用图例
        chart.legend.isEnabled = false

        // 禁用缩放功能
        chart.setScaleEnabled(false)

        //禁用其坐标轴
        xAxis.setDrawAxisLine(false)
        leftAxis.setDrawAxisLine(false)
        //网格设置
        xAxis.setDrawGridLines(false)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridLineWidth=1f

        // 刷新图表
        chart.invalidate()
    }

    /**
     * function:设置一个限制的辅助线
     * @param：（1）轴 （2）位置 （3）颜色
     * */
    private fun addLimitLine(mAxis: AxisBase,pos:Float,color:Int) {
        val ll = LimitLine(pos)
        ll.lineColor = color
        ll.lineWidth = 2f
        ll.enableDashedLine(30f,30f,0f)
        mAxis.addLimitLine(ll)
    }

    /**
     * function:创造并填充两条限制线之间的颜色
     * @param:(1)图表（2）起始（3）结束（4）填充颜色
     * */

    private fun addMultipleVerticalLimitLinesAndFillAreas(
        lineChart: LineChart,
        areas: List<AreaInfo>
    ) {
        // 创建并添加限制线
        areas.forEach { area ->
            val limitLineStart = LimitLine(area.start, "")
            limitLineStart.lineColor = area.color
            lineChart.axisLeft.addLimitLine(limitLineStart)

            val limitLineEnd = LimitLine(area.end, "")
            limitLineEnd.lineColor = area.color
            lineChart.axisLeft.addLimitLine(limitLineEnd)
        }

        // 设置图表背景为自定义 Drawable 以填充多个区域
        lineChart.background = object : Drawable() {
            override fun draw(@NonNull canvas: Canvas) {
                // 创建画笔
                val paint = Paint()
                // 设置画笔样式为填充
                paint.style = Paint.Style.FILL

                // 获取 X 轴的最小值
                val xChartMin = lineChart.xChartMin
                // 获取 X 轴的最大值
                val xChartMax = lineChart.xChartMax

                // 获取 X 轴的最小值对应的屏幕横坐标
                val xMin = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
                    .getPixelForValues(xChartMin, 0f).x
                // 获取 X 轴的最大值对应的屏幕横坐标
                val xMax = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
                    .getPixelForValues(xChartMax, 0f).x

                // 遍历每个区域并绘制
                areas.forEach { area ->
                    // 设置画笔颜色
                    paint.color = area.color
                    // 设置画笔透明度
                    paint.alpha = 80

                    // 获取区域起始值对应的屏幕纵坐标
                    val yStart = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
                        .getPixelForValues(0f, area.start).y
                    // 获取区域结束值对应的屏幕纵坐标
                    val yEnd = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
                        .getPixelForValues(0f, area.end).y

                    // 在画布上绘制矩形，填充区域
                    canvas.drawRect(xMin.toFloat(), yStart.toFloat(), xMax.toFloat(), yEnd.toFloat(), paint)
                }
            }

            override fun setAlpha(alpha: Int) {
                // 暂时不需要实现
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                // 暂时不需要实现
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int {
                return android.graphics.PixelFormat.TRANSLUCENT
            }
        }
    }

    // 定义一个数据类来存储每个区域的信息
    data class AreaInfo(val start: Float, val end: Float, val color: Int)

}