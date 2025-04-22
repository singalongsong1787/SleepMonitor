package com.morales.bnatest

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.morales.bnatest.SensorForegroundService.Companion.ACTION_SENSOR_DATA
import com.morales.bnatest.SensorForegroundService.Companion.EXTRA_X
import com.morales.bnatest.SensorForegroundService.Companion.EXTRA_Y
import com.morales.bnatest.SensorForegroundService.Companion.EXTRA_Z
import kotlin.concurrent.thread

class alarmLightAwake : Service(){

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SENSOR_DATA) {
                // 在子线程中处理数据（避免阻塞主线程）
                thread {
                    val x = intent.getFloatExtra(EXTRA_X, 0f)
                    val y = intent.getFloatExtra(EXTRA_Y, 0f)
                    val z = intent.getFloatExtra(EXTRA_Z, 0f)

                    // 打印日志（直接输出到控制台）
                    Log.d("AlarmLightAwakeService", "Received sensor data: X=$x, Y=$y, Z=$z")

                    processData(this@alarmLightAwake,x,y,z)
                    var isAwake  = getResult(this@alarmLightAwake)
                    Log.d("alarmLightAwake", "是否为觉醒${isAwake}")
                    //如何监测到为true之后——
                    /**
                     *（1）打开alarm_open活动
                     *（2）关闭掉这个服务
                     * */

                }
            }
        }
    }




    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "服务已启动")
        // 注册广播接收器
        val filter = IntentFilter(ACTION_SENSOR_DATA)
        registerReceiver(sensorDataReceiver, filter)
    }

    // 空实现：如果不需要绑定服务，返回 null
    override fun onBind(intent: Intent?): IBinder? = null

    private fun processData(context: Context, x: Float, y: Float, z: Float) {  // 添加context参数
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))  // 使用传入的context
            }
            val python = Python.getInstance()
            val pyModule = python.getModule("light_awake")
            pyModule.callAttr("diff_data", x.toDouble(), y.toDouble(), z.toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "调用 Python 处理失败（发送数据到python端）", Toast.LENGTH_SHORT).show()  // 使用context
        }
    }

    private fun getResult(context: Context):Boolean {  // 添加context参数
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))  // 使用传入的context
            }
            val python = Python.getInstance()
            val pyModule = python.getModule("light_awake")
            val flag = pyModule.callAttr("judge_data")
            return flag.toBoolean()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AlarmService", "计算觉醒数据（接收Python）出错: ${e.message}")
            Toast.makeText(context, "计算觉醒数据（接收Python）", Toast.LENGTH_SHORT).show()  // 使用context
            return false
        }
    }

}