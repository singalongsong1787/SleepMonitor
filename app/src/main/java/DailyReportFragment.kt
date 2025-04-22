package com.morales.bnatest.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.morales.bnatest.R
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.morales.bnatest.ScreenShot
import com.morales.bnatest.SleepData
import com.morales.bnatest.SpacingItemDecoration
import com.morales.bnatest.adapter.SnoringAdapter
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

/**
* DailyReportFragment是一个简单的Fragment,用于显示日常的报告界面
 * 主要功能是加载并显示布局文件fragment_daily_report
 * */

/*
class DailyReportFragment : Fragment() {
    /**
     * 在Fragment创建视图并加载布局文件fragment_daily_report
     * @param inflater 用于将布局文件转换为 View 对象的工具。
     * @param container 父视图容器，用于存放加载后的视图。
     * @param savedInstanceState 保存的实例状态，用于恢复 Fragment 的状态。
     */

    private val TAG = "DailyReportFragment"

    // 用于显示鼾声数据的 RecyclerView
    private lateinit var snoringView: RecyclerView
    // 自定义的 RecyclerView 适配器
    private lateinit var snoringAdapter: SnoringAdapter
    // 存储文件名的列表
    private lateinit var fileNames: List<String>

    // 注：onCreateView 方法是 Fragment 生命周期中的一个重要方法，用于创建和返回 Fragment 的视图。
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: DailyReportFragment's view is being created.")

        // 使用 LayoutInflater 从布局文件 fragment_daily_report.xml 中创建视图
        val view = inflater.inflate(R.layout.fragment_daily_report, container, false)
        // 通过 findViewById 方法获取 RecyclerView 组件
        snoringView = view.findViewById(R.id.SnoringView)
        // 为 RecyclerView 设置布局管理器，这里使用线性布局管理器
        snoringView.layoutManager = LinearLayoutManager(requireContext())

        // 调用 getFileNames 方法获取文件列表
        fileNames = getFileNames()
        Log.d(TAG, "File names size: ${fileNames.size}")

        fileNames.forEachIndexed { index, fileName ->
            Log.d(TAG, "File name at index $index: $fileName")
        }

        // 创建 SnoringAdapter 实例，并将上下文和文件列表传递给适配器
        snoringAdapter = SnoringAdapter(requireContext(), fileNames)

        // 将适配器设置给 RecyclerView，用于显示数据
        snoringView.adapter = snoringAdapter

        return view
    }

    // 获取指定目录下的所有文件名
    private fun getFileNames(): List<String> {
        Log.d("DailyReportFragment", "getFileNames called")
        // 创建一个可变列表，用于存储文件名
        val names = mutableListOf<String>()
        // 获取应用内部存储的文件目录下的 2025-03-06 目录
        val directory = File(requireContext().filesDir, "2025-03-06")
        Log.d("DailyReportFragment", "Directory path: ${directory.absolutePath}")
        // 检查目录是否存在且为目录类型
        if (directory.exists() && directory.isDirectory) {
            Log.d("DailyReportFragment", "Directory exists and is a directory")
            // 获取目录下的所有文件
            val files = directory.listFiles()
            if (files != null) {
                Log.d("DailyReportFragment", "Files found in directory: ${files.size}")
                // 遍历文件数组
                files.forEach { file ->
                    names.add(file.name)
                }
            } else {
                Log.e("DailyReportFragment", "No files found in directory")
            }
        } else {
            Log.e("DailyReportFragment", "Directory does not exist or is not a directory")
        }
        return names
    }
}*/

// 定义 DailyReportFragment 类，继承自 Fragment，用于显示每日报告界面
class DailyReportFragment : Fragment() {

    // 用于显示鼾声数据的 RecyclerView
    private lateinit var snoringView: RecyclerView
    // 自定义的 RecyclerView 适配器
    private lateinit var snoringAdapter: SnoringAdapter
    // 存储文件名的列表
    private lateinit var fileNames: List<String>
    // 存储文件完整路径的列表
    private lateinit var filePaths: List<String>

    // 存储原始文件名的列表
    private lateinit var originalFileNames: List<String>
    // 存储处理后的时间信息的列表
    private lateinit var displayFileNames: List<String>
    //定义图片
    private lateinit var icon_screenShot:ImageButton


    // 创建 Fragment 的视图
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DailyReportFragment", "onCreateView called")
        // 使用 LayoutInflater 从布局文件 fragment_daily_report.xml 中创建视图
        val view = inflater.inflate(R.layout.fragment_daily_report, container, false)
        // 通过 findViewById 方法获取 RecyclerView 组件
        snoringView = view.findViewById(R.id.SnoringView)
        // 为 RecyclerView 设置布局管理器，这里使用线性布局管理器
        snoringView.layoutManager = LinearLayoutManager(requireContext())



        val spacing = 16
        snoringView.addItemDecoration(SpacingItemDecoration(spacing))


        /*
        // 调用 getFileNames 方法获取文件列表
        fileNames = getFileNames()
        Log.d("DailyReportFragment", "File names size: ${fileNames.size}")
        fileNames.forEachIndexed { index, fileName ->
            Log.d("DailyReportFragment", "File name at index $index: $fileName")
        }*/

        val fileInfo = getFileNames()
        originalFileNames = fileInfo.first
        displayFileNames = fileInfo.second
        filePaths = fileInfo.third

        Log.d("DailyReportFragment", "Original file names size: ${originalFileNames.size}")
        originalFileNames.forEachIndexed { index, fileName ->
            Log.d("DailyReportFragment", "Original file name at index $index: $fileName")
        }


        // 创建 SnoringAdapter 实例，并将上下文、原始文件名、处理后的文件名和文件路径传递给适配器
        snoringAdapter = SnoringAdapter(requireContext(), originalFileNames, displayFileNames, filePaths, snoringView)

        // 将适配器设置给 RecyclerView，用于显示数据
        snoringView.adapter = snoringAdapter



        return view
    }

    /**
     * function:获取文件名
     * @param：无
     * @return：三元组（1）原始文件名（2）时间信息（3）文件的文件路径
     * */

    // 获取指定目录下的所有文件名
    private fun getFileNames(): Triple<List<String>, List<String>, List<String>> {
        Log.d("DailyReportFragment", "getFileNames called")
        // 创建可变列表，用于存储原始文件名、处理后的时间信息和文件完整路径
        val originalNames = mutableListOf<String>()//存储原始文件名
        val displayNames = mutableListOf<String>()//存储处理的时间信息
        val paths = mutableListOf<String>()//文件的完整路径

        /*获得最近的日期*/
        // 获取 SharedPreferences 文件
        val sharedPreferences = requireContext().getSharedPreferences("SleepDataPrefs", Context.MODE_PRIVATE)
        // 获取所有键值对
        val allEntries = sharedPreferences.all
        // 用于存储最近的日期
        var latestDate: String? = null

        // 遍历所有键值对，找到最近的日期
        for (entry in allEntries) {
            if (entry.key.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                if (latestDate == null || entry.key > latestDate) {
                    latestDate = entry.key
                }
            }
        }

        // 获取应用内部存储的文件目录下的 2025-03-06 目录
        //注意：这个函数未来可以改接收一个参数的形式，放在时间这里

        if(latestDate != null){
            val directory = File(requireContext().filesDir, latestDate)
            Log.d("DailyReportFragment", "Directory path: ${directory.absolutePath}")
            // 检查目录是否存在且为目录类型
            if (directory.exists() && directory.isDirectory) {
                Log.d("DailyReportFragment", "Directory exists and is a directory")
                // 获取目录下的所有文件
                val files = directory.listFiles()
                if (files != null) {
                    Log.d("DailyReportFragment", "Files found in directory: ${files.size}")
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
                    Log.e("DailyReportFragment", "No files found in directory")
                }
            } else {
                Log.e("DailyReportFragment", "Directory does not exist or is not a directory")
            }
        }
        return Triple(originalNames, displayNames, paths)
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap) {
        try {
            // 获取外部存储的公共图片目录
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            Log.d("Screen","存放的目录为${storageDir}")
            if (storageDir != null) {
                // 创建一个图片文件
                val file = File(storageDir, "screenshot_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(file)
                // 将 Bitmap 压缩为 PNG 格式并保存到文件中
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

