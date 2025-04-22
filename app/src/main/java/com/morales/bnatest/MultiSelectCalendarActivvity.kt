package com.morales.bnatest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MultiSelectCalendarActivity : AppCompatActivity(){

    private lateinit var calendarView:MaterialCalendarView //日历视图变量
    private lateinit var confirmButton: Button             //日历按键变量

    //对逻辑初始化函数onCreate进行复写
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar2)// 对视图进行展示

        calendarView = findViewById(R.id.calendarView2)
        confirmButton = findViewById(R.id.confirmButton)

        // 读取 SharedPreferences 中的可选日期
        val selectableDates = getSelectableDatesFromSharedPrefs()

        // 创建并应用装饰器
        calendarView.addDecorator(RedDateDecorator(selectableDates))
        calendarView.addDecorator(SelectableDatesDecorator(selectableDates))

        /*
        // 设置日期选择监听器
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            // 可以在这里处理日期选择变化的逻辑
        })*/

        // 设置确认按钮点击监听器
        confirmButton.setOnClickListener {
            val selectedDates = calendarView.selectedDates
            val selectedDateInfo = selectedDates.map { "${it.year}-${it.month }-${it.day}" }
            Log.d("MultiSelectCalendar", "选中的日期列表: ${selectedDateInfo.joinToString()}")

            // 创建 AlertDialog 对话框
            val builder = AlertDialog.Builder(this)
            builder.setTitle("This is Dialog")
            builder.setMessage("是否删除所选数据")

            // 设置取消按钮
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss() // 关闭对话框
            }

            // 设置确定按钮
            builder.setPositiveButton("OK") { dialog, which ->

                for (date in selectedDates) {
                    val dateKey = String.format(Locale.getDefault(), "%d-%02d-%02d", date.year, date.month , date.day)
                    Log.d("dateKey","删除的日期为：${dateKey}")
                    // 删除对应日期的文件夹
                    deleteDateFolder(dateKey)
                    // 删除 SharedPreferences 中对应日期的信息
                    deleteDataFromSharedPrefs(dateKey,"SleepDataPrefs")
                    deleteDataFromSharedPrefs(dateKey,"WakeUpAndDeepSleep")
                    // 显示删除成功的 Toast 提示
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()

                }
                dialog.dismiss() // 关闭对话框

            }

            // 显示对话框
            val dialog = builder.create()
            dialog.show()

        }
    }


    /**
     * function:可选择的日期
     * */
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

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)+1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val calendarDay = CalendarDay.from(year, month, day)
                selectableDates.add(calendarDay)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return selectableDates
    }

    /**
     * function:删除指定日期的文件夹及其内容
     * @param：指定的日期字符串，用于标识文件夹
     * */
    private fun deleteDateFolder(dateKey: String) {
        //获取应用的内部文件目录
        val appFilesDir = filesDir
        // 根据日期字符串构建目标文件夹的路径
        val dateFolder = File(appFilesDir, dateKey)
        // 检查目标文件夹是否存在且是一个目录
        if (dateFolder.exists() && dateFolder.isDirectory) {
            deleteRecursive(dateFolder)
        }
    }


    /**
     * function:表示这是一个递归删除的方法。
     * @param:file
     * */
    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (child in fileOrDirectory.listFiles()!!) {
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }

    /**
     * 删除sharedPerferenced数据
     * */
    private fun deleteDataFromSharedPrefs(dateKey: String,file:String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(file, Context.MODE_PRIVATE)
        // 检查 SharedPreferences 中是否包含该日期的键
        val isDateExists = sharedPreferences.contains(dateKey)
        if (isDateExists) {
            Log.d("MultiSelectCalendar", "SharedPreferences 中存在日期 $dateKey，准备删除...")
            val editor = sharedPreferences.edit()
            editor.remove(dateKey)
            editor.apply()
            Log.d("MultiSelectCalendar", "日期 $dateKey 已从 SharedPreferences 中删除")
        } else {
            Log.d("MultiSelectCalendar", "SharedPreferences 中不存在日期 $dateKey，无需删除")
        }
    }



}




