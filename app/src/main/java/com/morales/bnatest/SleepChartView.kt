package com.morales.bnatest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.*
import android.util.Log
import android.util.TypedValue
import android.util.Xml
import androidx.core.content.ContextCompat
import kotlinx.serialization.json.Json
import java.util.regex.Pattern
import android.view.MotionEvent
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.IOException


//先构造一个wakeUpDeepsleep的类
//只能通过读取文件来获取
/*
val class_example = wakeUpAndDeepSleep(

)*/


class SleepChartView @JvmOverloads constructor( //@JvmOverloads constructor用于生成多个重载的构造函数
    context: Context,//上下文对象，用于访问应用程序的资源、主题和其他全局信息
    attrs: AttributeSet? = null,//用于传递布局文件中定义的自定义属性
    defStyleAttr: Int = 0,//指定默认的样式属性
    // 添加数据类作为构造参数
    public var data: wakeUpAndDeepSleep? = null,//传递给视图的睡眠数据

    private val specifiedDate: String? = null


) : View(context, attrs, defStyleAttr) {


    init {
        data = getWakeUpAndDeepSleep(context,specifiedDate)
        isClickable = true
        isFocusable = true
    }


    // 创建画笔对象，用于绘制文本，设置抗锯齿、颜色、文本大小和对齐方式
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER  //文本将以指定的坐标点为中心进行绘制对其方式
    }

    //定义左边距和右边距
    private val leftRightMargin = dpToPx(40f)

    private val bottomMargin = dpToPx(20f)

    //声明这个变量，以便能够全局使用
    private var startTime: Long = 0
    private var endTime: Long = 0

    //为了让坐标轴更加美观，起止时刻加点偏移
    private val offsetX = dpToPx(10f)

    private var offsetX_touch = 0f


    private var lastX = 0f

    private var isDragging = false

    //翻身预测
    private var rollLine = 2f

    private val marginOfRollAx = dpToPx(20f)

    //重写onDraw方法，用于在视图上绘制内容
    override fun onDraw(canvas: Canvas){
        super.onDraw(canvas)

        canvas.save()




        data?.let{
            //解析时间
            //val (startTime,endTime) = parseTime(it.start_end)//解析起始时间


            val timePair = parseTime(it.start_end)
            startTime = timePair.first
            endTime = timePair.second


            //执行函数，计算起止之间的分钟差
            val totalMinutes = getMinutesDiff(startTime,endTime)


            val intervals_wakeUp = parseIntervalList(it.wakeup_interval,totalMinutes)


            val intervals_deepSleep = parseIntervalList(it.deepsleep_interval,totalMinutes)



            //绘制起始时间
            //1.画布，开始时间，x坐标轴
            drawTime(canvas, startTime, (leftRightMargin - 2*offsetX).toFloat())
            drawTime(canvas, endTime, (width - leftRightMargin + 2*offsetX).toFloat())

            //绘制中间数字
            drawMiddleNumbers(canvas, totalMinutes,startTime)



            /**-------------------------绘制条形图----------------------**/
            //初始化画布，绘制浅睡
            val PosLeft_lightSleep = leftRightMargin
            val PosTop_lightSleep = height-(height / 7) * 4
            val PosRight_lightSleep = width - leftRightMargin
            val PosBottom_lightSleep = height - bottomMargin - 50

            //深睡的坐标参数
            val PosTop_deepSleep = height-(height / 7) * 5
            val posBottom_deepSleep =  height - bottomMargin - 50

            //觉醒的坐标参数
            val PosTop_wakeUp = height-(height / 7) * 3
            val posBottom_wakeUp =  height - bottomMargin - 50

            //获取颜色资源值
            val paint_lightSleep:Paint = paint
            val color_lightSleep = ContextCompat.getColor(context, R.color.lightSleep)
            paint_lightSleep.color = color_lightSleep
            canvas.drawRect(PosLeft_lightSleep.toFloat(),PosTop_lightSleep.toFloat(),PosRight_lightSleep.toFloat(),PosBottom_lightSleep.toFloat(),paint_lightSleep)

            //绘制深睡
            val paint_deepSleep:Paint = paint
            paint_deepSleep.color = Color.WHITE
            drawRect(intervals_deepSleep,PosTop_deepSleep.toFloat(),PosBottom_lightSleep.toFloat(),paint,canvas)


            val color_deepSleep = ContextCompat.getColor(context, R.color.deepSleep)
            paint.color = color_deepSleep
            drawRect(intervals_deepSleep,PosTop_deepSleep.toFloat(),PosBottom_lightSleep.toFloat(),paint_deepSleep,canvas)

            //绘制觉醒
            val paint_wakeUp1:Paint = paint
            val color_wakeUp1 = ContextCompat.getColor(context, R.color.daily_header)
            paint_wakeUp1.color = color_wakeUp1
            drawRect(intervals_wakeUp,PosTop_lightSleep.toFloat(),PosBottom_lightSleep.toFloat(),paint_wakeUp1,canvas)

            val paint_wakeUp:Paint = paint
            val color_wakeUp = ContextCompat.getColor(context, R.color.wakeUp)
            paint_wakeUp.color = color_wakeUp
            drawRect(intervals_wakeUp,PosTop_wakeUp.toFloat(),posBottom_wakeUp.toFloat(),paint_wakeUp,canvas)

            //绘制直线
            val xlinePos = leftRightMargin
            val heightline = (height / 7) * 4.5
            val color_line = ContextCompat.getColor(context, R.color.line_red)
            paint.color = color_line
            paint.strokeWidth = 5f
            val line = drawLine(xlinePos.toFloat(),heightline.toFloat(),paint,canvas,offsetX_touch)

            //绘制时间文本框
            val textOfTime_width = 150f
            val textOfTime_left = line.startX - textOfTime_width / 2
            val textOfTime_height = 75f
            val textOfTime_bottom = line.stopY

            //新建一个paint
            val paint_RectOfTime:Paint = paint
            val color_textOfTime = ContextCompat.getColor(context, R.color.textOfTime)
            paint_RectOfTime.color = color_textOfTime
            val rectOfTime = drawRectOfTime(textOfTime_left,textOfTime_width,textOfTime_height,textOfTime_bottom,paint_RectOfTime,
                canvas,20f)

            //绘制文本时间
            var textOfTime = "00:00"
            val paint_textOfTime = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 40f
                textAlign = Paint.Align.CENTER  //文本将以指定的坐标点为中心进行绘制对其方式
            }
            drawTextOfTime(line,rectOfTime,paint_textOfTime,canvas)

            //画一条直线横贯开始时间和结束时间

            val paint_roll:Paint = paint
            val color_roll = ContextCompat.getColor(context, R.color.roll_View)
            paint_roll.color = color_roll

            canvas.drawLine((leftRightMargin).toFloat(),(height-bottomMargin+marginOfRollAx).toFloat(),
                (width - leftRightMargin).toFloat(),(height-bottomMargin+marginOfRollAx).toFloat(),paint_roll)

            //绘制文本
            val paint_rollTex = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 40f
                textAlign = Paint.Align.CENTER  //文本将以指定的坐标点为中心进行绘制对其方式
            }
            canvas.drawText("翻身",(leftRightMargin - 2*offsetX).toFloat(),(height-bottomMargin+marginOfRollAx).toFloat(),paint_rollTex)


            //解析文件
            val roll_list = parseSharedPreferencesXML(context,"StatusOfRollPrefs_1")
            Log.d("SleepChartView","parse后的list:${roll_list}")
            for((time,value) in roll_list){
                //Log.d("SleepChart","时间的分钟值为${getSecsToMintues(time)}")
                var timeOfRoll_parse = getSecsToMintues(time)
                //将其转换为View上的坐标
                var xPos_Roll = (timeOfRoll_parse - startTime) / totalMinutes *(width - 2 * leftRightMargin) + leftRightMargin
                drawlineOfRoll(xPos_Roll,100f,paint_roll,canvas)
                Log.d("SleepChartView","绘制成功")
            }



        }
    }

    /**
     * function:解析时间，返回起始时间和结束时间的Pair
     * @param:时间标签
     * @return: （1）起始时间 （2）结束时间
     * */
    /*
    private fun parseTime(timeStr: String): Pair<Long, Long> {
        // 移除多余字符，如 [、'、(、) 等
        //我觉得这个最后也是要改的
        val cleanStr = timeStr.replace("[\\[\\]'()]".toRegex(), "")
        val parts = cleanStr.split(", ")
        return Pair(
            getMinutes(parts[0]),
            getMinutes(parts[1])
        )
    }*/

    private fun parseTime(timeList:List<List<String>>):Pair<Long,Long>{

        val timePair = timeList.firstOrNull()
        if (timePair != null && timePair.size == 2) {
            val startTime = getMinutes(timePair[0])
            val endTime = getMinutes(timePair[1])
            return Pair(startTime, endTime)
        }
       return Pair(0,0)
    }

    /**
     * function：将时间串转换为分钟数(String -> Long)
     * */
    private fun getMinutes(time: String): Long {
        val parts = time.split(":")
        return (parts[0].toLong() * 60 + parts[1].toLong())
    }


    /**
     * function:将时间串转换成秒数(String -> Long)
     * */
    private fun getSecsToMintues(time:String):Float{
        val parts = time.split(":")
        Log.d("SleepCharts","秒为${parts[2]}")
        return(parts[0].toLong() * 60 + parts[1].toLong() +(parts[2].toFloat() / 60))
    }

    /**
     * function:得到其中的时间差别
     * */
    private fun getMinutesDiff(start: Long, end: Long): Long {
        var diff = end - start
        if(diff < 0){
            diff += 1440
        }
        return diff
    }

    /**
     * function:在指定时刻绘制时间文本（主要是起止时刻）
     * */
    private fun drawTime(canvas: Canvas, minutes: Long, x: Float) {
        val h = (minutes / 60).toInt()
        val m = (minutes % 60).toInt()
        // 格式化为时间字符串（如 "10:30"）
        val timeText = String.format("%02d:%02d", h, m)
        // 在画布上绘制时间文本
        canvas.drawText(timeText, x, (height - bottomMargin).toFloat(), paint)
    }

    /**
     * function:绘制中间数字
     * @param：（1）画布 （2）总的分钟数 (3)开始时间
     * */
    /*
    private fun drawMiddleNumbers(canvas: Canvas, totalMinutes: Long) {
        val numberCount = 7 // 示例数字1-7，可根据实际需求调整
        val unitWidth = measuredWidth / (totalMinutes.toFloat())
        for (i in 1..numberCount) {
            val minutePos = (totalMinutes / (numberCount + 1) * i)
            val xPos = minutePos.toFloat() * unitWidth
            canvas.drawText(i.toString(), xPos,  (height - bottomMargin).toFloat(), paint)
        }
    }*/

    private fun drawMiddleNumbers(canvas: Canvas, totalMinutes: Long, startTime: Long){

        //用于保存整点
        val hourlist = mutableListOf<Int>()
        //用于保存整点对应的x坐标
        val xPosList = mutableListOf<Float>()

        //计算开始时间对应的小时
        val startHour = (startTime/60).toInt() //比如23
        //计算结束时间对应的小时
        var endHour = ((startTime+totalMinutes)/60).toInt() //需要做一个judge //8->32
        //事先+24
        if(endHour < startHour){
            endHour += 24
        }


        //遍历开始到结束的每一个整点
        for (hour in startHour..endHour){

            var adjustHour = hour
            //计算整点对应的分钟数
            val currentMinute = (hour*60).toLong()

            if (currentMinute >= startTime && currentMinute <= startTime + totalMinutes){

                //计算该整点距离开始时间的分钟数
                val minutesFromStart = currentMinute - startTime
                //计算该整点对应的x坐标
                val xPos = (minutesFromStart.toFloat() / totalMinutes) * (width - 2 * leftRightMargin) + leftRightMargin //可以

                if(adjustHour >= 24){
                    adjustHour -= 24
                }

                //保存相关内容
                hourlist.add(adjustHour)

                xPosList.add(xPos)

            }

        }

        //绘制中间的数字
        for (i in hourlist.indices) {
            val hour = hourlist[i]
            val xPos = xPosList[i]
            // 在画布上绘制整点数字，距离底部 20dp
            canvas.drawText(hour.toString(), xPos, (height - bottomMargin).toFloat(), paint)
        }

    }

    /**
     * function:自定义视图的测量逻辑
     * */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredHeight = (paint.textSize * 3 + 40).toInt()
        setMeasuredDimension(
            resolveSize(desiredHeight, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    /**
     * function:获取三个区间即wakeUp，lightSleep和deepSleep
     */

    /**
     * function:对区间进行解析
     * @param:区间Interval
     * @return:解析后的区间
     * */

    /*
    private fun parseIntervals(intervalStr: String): List<Pair<Double, Double>> {

        val intervals = mutableListOf<Pair<Double, Double>>()//存储用的


    }*/

    /**
     * function:将设备像素（dp）转换为像素（px）
     * @param：dp值
     * @return：像素值
     * */
    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }



    /*
    private fun getWakeUpAndDeepSleep(context: Context): wakeUpAndDeepSleep? {
        //获取名为”SleepDataPrefs“的sharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("WakeUpAndDeepSleep", Context.MODE_PRIVATE)
        //获取所有条目
        val allEntries = sharedPreferences.all
        //用于存储找到的最新日期
        var latestDate: String? = null

        // 遍历SharedPreferences中的所有键值对
        for (entry in allEntries) {

            if (Pattern.matches("\\d{4}-\\d{2}-\\d{2}", entry.key)) {
                if (latestDate == null || entry.key > latestDate) {
                    latestDate = entry.key
                }
            }
        }
        Log.d("wakeUpAndDeepSleep", "latestDate: $latestDate")

        /**
         * 注：这的函数语法得好好看看
         * */

        return latestDate?.let { date ->
            try {
                // 获取对应日期的 JSON 字符串
                val jsonString = sharedPreferences.getString(date, null)!!
                    // 使用 kotlinx.serialization 解析 JSON 字符串
                    val json = Json { ignoreUnknownKeys = true }
                    return@let json.decodeFromString<wakeUpAndDeepSleep>(jsonString)
                    Log.d("wakeUpAndDeepSleep","Json解析成功")

                } catch(e: Exception) {
                Log.e("SleepData", "解析 JSON 数据出错: ${e.message}", e)
                }
                null

            }


        }*/


    private fun getWakeUpAndDeepSleep(context: Context, specifiedDate: String? = null): wakeUpAndDeepSleep? {
        //获取名为”SleepDataPrefs“的sharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("WakeUpAndDeepSleep", Context.MODE_PRIVATE)
        //获取所有条目
        val allEntries = sharedPreferences.all
        //用于存储找到的最新日期
        var latestDate: String? = null
        var targetDate: String? = null

        // 遍历SharedPreferences中的所有键值对
        for (entry in allEntries) {
            if (Pattern.matches("\\d{4}-\\d{2}-\\d{2}", entry.key)) {
                if (latestDate == null || entry.key > latestDate) {
                    latestDate = entry.key
                }
            }
        }


        // 判断是否传入了指定日期
        if (specifiedDate != null) {
            targetDate = specifiedDate
        } else {
            targetDate = latestDate
        }

        return targetDate?.let { date ->
            try {
                // 获取对应日期的 JSON 字符串
                val jsonString = sharedPreferences.getString(date, null)
                if (jsonString != null) {
                    // 使用 kotlinx.serialization 解析 JSON 字符串
                    val json = Json { ignoreUnknownKeys = true }
                    val result = json.decodeFromString<wakeUpAndDeepSleep>(jsonString)
                   // Log.d("wakeUpAndDeepSleep", "Json解析成功")
                    return@let result
                }
            } catch (e: Exception) {
                //Log.e("SleepData", "解析 JSON 数据出错: ${e.message}", e)
            }
            null
        }
    }


    /**
     * function:对一个区间列表进行解析（通过getMinutes换算成分钟度量）
     *          注：对WakeUp和DeepSleep进行转换
     * @param：区间列表List<List<String>>
     * @return:区间列表List<List<Long>>
     * */
    private fun parseIntervalList(intervalList: List<List<String>>,totalMinutes: Long): List<List<Float>> {
        return intervalList.map { interval ->
            interval.map { time ->
                ( (getMinutes(time).toFloat() - startTime) / totalMinutes) * (width - 2*leftRightMargin) + leftRightMargin
            }
        }
    }

    /**
     * function:初始化画布，绘制深睡
     * @param：（1）区间列表，解析之后 （2）top (3)bottom (4)画笔
     * @return：无
     * */

    private fun drawRect(intervalList: List<List<Float>>, top:Float, bottom:Float, paint_draw:Paint, canvas:Canvas){

        intervalList.forEach{interval ->
            val left = interval[0]
            val right = interval[1]
            canvas.drawRect(left,top,right,bottom,paint)
            canvas.drawRect(left,top,right,bottom,paint_draw)
        }

    }

    /**
     * function:创建一条直线
     * @param：（1）起始xleft（2）高度(3）画笔（4）canvas
     * @return：（1）线的对象
     * */

    private fun drawLine(xPos:Float,heightline:Float,paint:Paint,canvas:Canvas,offsetX_touch:Float):Line{
        val startX = xPos + offsetX_touch
        val startY = (height - bottomMargin).toFloat() -50
        val stopX = xPos + offsetX_touch
        val stopY = (startY - heightline)

        canvas.drawLine(startX,startY,stopX,stopY,paint)
        //能够返回其对象
        val line =  Line(startX, startY, stopX, stopY)
        return line
    }
    private fun drawlineOfRoll(xPos:Float,intentsity:Float,paint:Paint,canvas:Canvas){
        val startX = xPos
        val startY =(height-bottomMargin+ marginOfRollAx) + intentsity / 2
        val stopX = xPos
        val stopY =(height-bottomMargin+ marginOfRollAx) - intentsity / 2
        canvas.drawLine(startX,startY,stopX,stopY,paint)
    }



    //画一个文本框（矩形框）
    /**
     * function:画出矩形框
     * @param：（1）左（2）宽（3）高（4）底（5）画笔（6）画布(7)圆角半径
     * @return:矩形对象
     * */
    private fun drawRectOfTime(xPosLeft:Float, width_rectOfTime:Float, height_rectOfTime:Float,
                               xPosBottom:Float,paint:Paint,canvas:Canvas,cornerRadius:Float):RectF{

        val xPosTop = xPosBottom - height_rectOfTime
        val xPosRight = xPosLeft + width_rectOfTime
        val rect = RectF(xPosLeft,xPosTop,xPosRight,xPosBottom)
        //绘制矩形
        canvas.drawRoundRect(rect,cornerRadius,cornerRadius,paint)
        return rect
    }

    /**
     * function:绘制时间文本
     * @param：(1)文件内容 （2）矩形类对象 （3）画笔 （4）画布
     * */
    private fun drawTextOfTime(line:Line,rect:RectF,paint:Paint,canvas:Canvas){

        //获取直线的起始点
        val line_startX = line.startX
        val distance = line_startX - leftRightMargin
        val clock = distanceToClock(distance)

        val textX = (rect.left + rect.right) / 2f
        val textY = (rect.top + rect.bottom) / 2f
        canvas.drawText(clock,textX,textY,paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val actionName = when (event.action) {
            MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
            MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
            MotionEvent.ACTION_UP -> "ACTION_UP"
            MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
            MotionEvent.ACTION_OUTSIDE -> "ACTION_OUTSIDE"
            // 可以根据需要添加更多的动作类型
            else -> "UNKNOWN_ACTION"
        }
        Log.d("onTouchEvent", "当前触摸事件动作: $actionName")

        // 根据触摸事件的类型处理不同的动作
        when (event.action) {
           MotionEvent.ACTION_DOWN -> {
                // 当用户首次触摸屏幕时触发
                // 记录触摸点的初始位置
               parent.requestDisallowInterceptTouchEvent(true)
                lastX = event.x
                isDragging = true
                Log.d("onTouchEvent","首次触摸屏幕触发时x坐标${lastX}")
            }
            MotionEvent.ACTION_MOVE -> {
                // 当用户移动触摸点时触发
                // 计算触摸点的移动距离
                if(isDragging) {

                    val dx = event.x - lastX // 水平方向的移动距离
                    Log.d("onTouchEvent", "移动触发时移动距离dx为${dx}")

                    // 更新偏移量
                    offsetX_touch += dx  // 累加水平方向的偏移量(每次我们都有偏移量，但跟手的移动不匹配，跟有上限的一样)
                    offsetX_touch = offsetX_touch.coerceIn(0f ,(width - 2 * leftRightMargin).toFloat())

                    //应该跟视图的大小有关
                    Log.d("onTouchEvent", "更新偏移量为x为${offsetX_touch}")
                    // 更新触摸点的当前位置
                    lastX = event.x

                    // 请求重新绘制视图，以反映新的偏移量
                    invalidate()
                }

            }

            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        // 表示事件已被处理
        // 表示事件已被处理
        val returnValue = true // 记录返回值
        Log.d("onTouchEvent", "onTouchEvent 方法返回值为: $returnValue") // 输出返回值
        return returnValue
    }

    /**
     * function:将分钟转换为时间文本
     * @param：分钟数
     * @return：时间文本
     * */
    private fun minutesToClock(min: Long): String{
        var minute = min
        if(min>1440){
            minute -= 1440
        }

        val hours = minute / 60
        val minutes = minute % 60
        return String.format("%02d:%2d",hours,minutes)
    }

    /**
     * function:将距离转换为时间
     * @param：距离
     * @return：时间
     * */

    private fun distanceToClock(distance: Float): String{

        //计算该时刻的分钟数
        val distance_min = distance / (width - 2*leftRightMargin) * (endTime - startTime)
        val minToClock = minutesToClock((distance_min+ startTime).toLong())
        return minToClock
    }

    /**
     * function:对data的改变做出监听
     * @param：一个新的data
     * @return：无
     * */

    fun updateData(newData: wakeUpAndDeepSleep?) {
        data = newData
        invalidate() // 触发重绘
    }


    private fun parseSharedPreferencesXML(context:Context,prefsName:String):List<Pair<String,Float>>{
       // val result = mutableListOf<Pair<String, Any>>()
        val entries = mutableListOf<Pair<String, Float>>()
        val prefsDir = File(context.filesDir.parent, "shared_prefs")
        val prefsFile = File(prefsDir, "$prefsName.xml")

        if (!prefsFile.exists()) {
            return entries
        }

        val parser: XmlPullParser = Xml.newPullParser()
        try {
            parser.setInput(FileInputStream(prefsFile), null)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "float") {
                    val name = parser.getAttributeValue(null, "name")
                    val valueStr = parser.getAttributeValue(null, "value")
                    val value = valueStr?.toFloatOrNull()
                    if (name != null && value != null) {
                        entries.add(Pair(name, value))
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return entries

    }

    //做一个直线对象的类
    inner  class Line(val startX: Float, val startY: Float, val stopX: Float, val stopY: Float)
}

