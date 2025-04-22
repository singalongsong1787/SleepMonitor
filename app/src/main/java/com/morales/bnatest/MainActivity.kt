package com.morales.bnatest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.morales.bnatest.databinding.ActivityMainBinding
import com.morales.bnatest.ui.home.HomeFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        navView.setupWithNavController(navController)


        val id = intent.getIntExtra("id", 0)

// 检查接收到的 id 是否等于 1
        if (id == 1) {
            // 获取 FragmentManager，开始一个 Fragment 事务
            supportFragmentManager
                .beginTransaction()
                // 将 R.id.fragment_container 容器中的内容替换为 YourFragment 的实例
                .replace(R.id.fragment_container, HomeFragment())
                // 将本次事务添加到返回栈，以便用户按返回键时可以回退
                .addToBackStack(null)
                // 提交事务，使更改生效
                .commit()
        }

    }
/*
    override fun onStop(){
        super.onStop()
        //应用进入后台时启动前台服务

        //定义函数，启动前台服务
        startForegroundService()
    }

    private fun startForegroundService() {
        //具体的启动前台服务——定于Intent，启动
        val intent = Intent(this, SensorForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // 其他方法保持不变
    private fun getAndLogIntensityLists() {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()
            val pyModule = python.getModule("sensor_data")
            val result: PyObject = pyModule.callAttr("get_intensity_lists")

            val intensityX = result.asList()[0].asList().map { it.toDouble() }
            val intensityY = result.asList()[1].asList().map { it.toDouble() }
            val intensityZ = result.asList()[2].asList().map { it.toDouble() }

            Log.d("IntensityLists", "IntensityX: $intensityX")
            Log.d("IntensityLists", "IntensityY: $intensityY")
            Log.d("IntensityLists", "IntensityZ: $intensityZ")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("IntensityLists", "获取 intensity 列表失败: ${e.message}")
        }
    }*/

}