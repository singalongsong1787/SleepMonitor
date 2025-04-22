package com.morales.bnatest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil.setContentView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morales.bnatest.adapter.SnoringAdapter
import java.io.File
import kotlin.math.min

class SnoringReportActivity: AppCompatActivity() {

    private lateinit var selectedDayTextView: TextView //"actionBar"下的时间TextView
    private lateinit var snoringView: RecyclerView     //recycleView
    private lateinit var snoringAdapter: SnoringAdapter //鼾声适配器
    private lateinit var originalFileNames: List<String> //当日原始文件名
    private lateinit var displayFileNames: List<String>//当日修改文件名
    private lateinit var filePaths: List<String>       //当日文件路径

    //对onCreate进行复写
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)//加载视图，即xml文件

        selectedDayTextView = findViewById(R.id.selectedDay)
        snoringView = findViewById(R.id.function_recycler_view2)

        val spacing = 16
        snoringView.addItemDecoration(SpacingItemDecoration(spacing))

        // 获取从日历页面传递过来的日期
        val selectedDate = intent.getStringExtra("selectedDate")
        selectedDayTextView.text = selectedDate



        // 获取指定日期的文件信息
        val fileInfo = if (selectedDate != null) {
            getFileNames(selectedDate)
        } else {
            // 处理 selectedDate 为 null 的情况，这里可以根据实际需求修改
            Triple(emptyList(), emptyList(), emptyList())
        }
        originalFileNames = fileInfo.first
        displayFileNames = fileInfo.second
        filePaths = fileInfo.third

        // 设置 RecyclerView 的布局管理器
        snoringView.layoutManager = LinearLayoutManager(this)

        // 创建 SnoringAdapter 实例，并将上下文、原始文件名、处理后的文件名和文件路径传递给适配器

        /**
         * 在 Activity 中，你可以直接使用 this 来代替 requireContext()，因为 Activity 本身就是一个 Context
         * requireContext() 是 Fragment 类中的一个方法，用于获取 Fragment 所依附的 Context
         * */

        snoringAdapter = SnoringAdapter(this, originalFileNames, displayFileNames, filePaths, snoringView,selectedDate)


        // 将适配器设置给 RecyclerView，用于显示数据
        snoringView.adapter = snoringAdapter
    }

    /**
     * function:获取文件名
     * @param：日期字符串
     * @return：三元组（1）原始文件名（2）时间信息（3）文件的文件路径
     * */
    private fun getFileNames(selectedDate: String): Triple<List<String>, List<String>, List<String>> {
        Log.d("SnoringReportActivity", "getFileNames called for date: $selectedDate")
        // 创建可变列表，用于存储原始文件名、处理后的时间信息和文件完整路径
        val originalNames = mutableListOf<String>()//存储原始文件名
        val displayNames = mutableListOf<String>()//存储处理的时间信息
        val paths = mutableListOf<String>()//文件的完整路径

        // 获取应用内部存储的文件目录下的指定日期目录
        val directory = File(this.filesDir, selectedDate)
        Log.d("SnoringReportActivity", "Directory path: ${directory.absolutePath}")
        // 检查目录是否存在且为目录类型
        if (directory.exists() && directory.isDirectory) {
            Log.d("SnoringReportActivity", "Directory exists and is a directory")
            // 获取目录下的所有文件
            val files = directory.listFiles()
            if (files != null) {
                Log.d("SnoringReportActivity", "Files found in directory: ${files.size}")
                // 遍历文件数组
                files.forEach { file ->
                    // 提取原始文件名
                    val originalFileName = file.name
                    originalNames.add(originalFileName)
                    // 提取文件名中的时间部分
                    val timePart = originalFileName.substringAfter("_").substringBefore(".pcm")
                    // 将时间部分从 HH-MM-SS 转换为 HH:mm 格式
                    val formattedTime = timePart.substring(0, 2) + ":" + timePart.substring(3, 5)
                    displayNames.add(formattedTime)
                    paths.add(file.absolutePath)
                }
            } else {
                Log.e("SnoringReportActivity", "No files found in directory")
            }
        } else {
            Log.e("SnoringReportActivity", "Directory does not exist or is not a directory")
        }
        return Triple(originalNames, displayNames, paths)
    }
}