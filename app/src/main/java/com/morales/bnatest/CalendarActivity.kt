package com.morales.bnatest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity:AppCompatActivity(){

    //定义MaterialCalendarView类的对象
    private lateinit var calendarView: MaterialCalendarView

    //实现一个“逻辑”初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)//选定父类

        setContentView(R.layout.activity_calendar)//设置视图

        calendarView = findViewById(R.id.calendarView)//绑定xml文件中的日历视图

        // 读取 SharedPreferences 中的可选日期
        val selectableDates = getSelectableDatesFromSharedPrefs()
        Log.d("CalendarActivity", "可选日期列表: ${selectableDates.joinToString { "${it.year}-${it.month + 1}-${it.day}" }}")

        // 创建并应用装饰器
        calendarView.addDecorator(RedDateDecorator(selectableDates))
        Log.d("color","修改成功")

        calendarView.addDecorator(SelectableDatesDecorator(selectableDates))


        //设置日期选择监听器

        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                Log.d("CalendarClick", "click: ${date.year}-${date.month}-${date.day}")

                // 将日期格式化为 yyyy-MM-dd
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", date.year, date.month, date.day)

                // 启动 SnoringReportActivity 并传递所选日期
                val intent = Intent(this, SnoringReportActivity::class.java)
                intent.putExtra("selectedDate", selectedDate)
                startActivity(intent)
            }
        })

    }

    private fun getSelectableDatesFromSharedPrefs(): List<CalendarDay> {
        val sharedPreferences: SharedPreferences = getSharedPreferences("SleepDataPrefs", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all
        val selectableDates = mutableListOf<CalendarDay>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for ((key, _) in allEntries) {
            try {
                val date = dateFormat.parse(key)
                val calendar = Calendar.getInstance()
                calendar.time = date

                // 直接从 Calendar 获取年、月、日
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // 注意：Calendar.MONTH 是从 0 开始的
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // 将年、月、日转换为 CalendarDay
                val calendarDay = CalendarDay.from(year, month, day)
                selectableDates.add(calendarDay)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return selectableDates
    }

}

// 标红装饰器
class RedDateDecorator(private val selectableDates: List<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        Log.d("selectbleDates", "可选日期列表: ${selectableDates.joinToString { "${it.year}-${it.month }-${it.day}" }}")
        return selectableDates.contains(day)

    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(ColorDrawable(Color.RED))
    }
}

