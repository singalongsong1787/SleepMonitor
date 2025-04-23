package com.morales.bnatest.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.morales.bnatest.R
import com.morales.bnatest.WakeUp1
import com.morales.bnatest.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.util.Log
import android.widget.ImageButton
import java.util.Calendar

import com.morales.bnatest.SleepData // 导入 SleepData 类
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.databinding.adapters.RatingBarBindingAdapter
import com.morales.bnatest.AlarmService
import com.morales.bnatest.RotationVectorForegroundService
import com.morales.bnatest.alarm


class HomeFragment : Fragment() {
    //_binding绑定全局变量
    private var _binding: FragmentHomeBinding? = null




    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!


    //请求码（虽然新 API 不需要请求码，但保留注释方便理解原逻辑）
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1

    //创建时钟部分
    private lateinit var timeTextView: TextView
    private val handler = Handler(Looper.getMainLooper()) //用于定时任务的handler，handler用于管理线程，handler意为处理器，looper循环
    private val updateInterval = 1000L //定义更新间隔，每秒更新一次

    //声明所储存的类
    private lateinit var currentSleepData:SleepData



    //创建匿名类，用于实现定时任务
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()//自定义函数
            handler.postDelayed(this, updateInterval)//function:延迟updatainterval后执行Runable
        }
    }

    // 使用 registerForActivityResult 注册权限请求结果监听器
    // 当用户对权限请求做出响应时，会回调这里的 lambda 表达式
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // 权限请求结果回调
        if (isGranted) {
            // 权限已授予，启动录音
            startRecording()
        }
    }

    /**
    function:用于初始化Fragment的视图和逻辑
    @param inflater 用于布局文件转换为View对象
    @param container 父视图容器
    @param savedInstanceState 保存实例的状态
    @return 返回初始化后的视图
    */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {    //继承关系
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)//创建一个ViewModel（用于数据管理）

        _binding = FragmentHomeBinding.inflate(inflater, container, false)//使用布局绑定工具将布局文件绑定到当前 Fragment
        //时钟部分
        timeTextView = binding.homeClock//时钟
        handler.post(updateTimeRunnable)//启动定时任务，更新时间

        val root: View = binding.root

        //找到按钮
        val wakeupButton = binding.wakeupButton

        val iconAlarmColck = binding.iconAlarm

        //设置按钮点击事件(设计监听器)
        wakeupButton.setOnClickListener {
            // 获取当前时间和日期
            val currentTime = getCurrentTime()
            val currentDate = getCurrentDate()
            // 打印日志
            Log.d("HomeFragment", "点击时间: 日期 - $currentDate, 时间 - $currentTime")

            // 创建 SleepData 对象
            currentSleepData = SleepData(
                date = currentDate,
                startTime = currentTime,
                endTime = "",
                sleepDuration = "",
                snoreDataPath = "",
                endDate = "",
                snoringDuration = ""
            )




            // 这是一个工具方法，用于检查当前应用是否已被授予某个权限。
            if (ContextCompat.checkSelfPermission(
                    requireContext(),//获取当前 Fragment 或 Activity 的上下文
                    Manifest.permission.RECORD_AUDIO //：表示录音权限
                ) != PackageManager.PERMISSION_GRANTED //表示权限已被授予。
            ) {
                // 使用新 API 发起权限请求
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                /*
                //获取当前的时间和日期
                val currentTime = getCurrentTime()
                val currentDate = getCurrentDate()
                // 打印日志
                Log.d("HomeFragment", "点击时间: 日期 - $currentDate, 时间 - $currentTime")


                //创建SleepData对象
                val currentSleepData = SleepData(
                    date = currentDate,
                    startTime = currentTime,
                    endTime = "", // 这里结束时间暂时为空，后续可根据实际情况更新
                    snoreDataPath = "" // 这里鼾声保存路径暂时为空，后续可根据实际情况更新
                )*/

                startRecording()//后面有函数实现
            }

            // 启动闹钟服务
            val serviceIntent = Intent(requireContext(), AlarmService::class.java)
            requireContext().startService(serviceIntent)
            val rotationVectorIntent = Intent(requireContext(),RotationVectorForegroundService::class.java)
            requireContext().startForegroundService(rotationVectorIntent)
            Log.d("RotationVector","服务开启")
        }

        /*****闹钟操作**********/
        iconAlarmColck.setOnClickListener(){
            Log.d("alarm","点击了下去")
            val intent = Intent(requireContext(), alarm::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //停止更新时间
        handler.removeCallbacks(updateTimeRunnable)
    }




    private fun updateTime() {
        // 获取当前时间
        val currentDate = Date()
        // 定义时间格式
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // 格式化时间
        val formattedDate = dateFormat.format(currentDate)
        // 在 TextView 中显示时间
        timeTextView.text = formattedDate
    }


    private fun startRecording() {

        //启动 WakeUp1  Activity 并开始录音
        val intent = Intent(requireContext(), WakeUp1::class.java)
        //向WakeUp活动传递数据
        intent.putExtra("sleepData", currentSleepData)

        startActivity(intent)
    }

    /**
     * function:获取当前时间
     * @return：时：分
     * */
    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }

    /**
     * function:获取当前日期
     * @return：年-月-日
     * */

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}