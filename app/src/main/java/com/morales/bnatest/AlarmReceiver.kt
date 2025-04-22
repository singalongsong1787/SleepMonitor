package com.morales.bnatest

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.registerReceiver
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 闹钟触发时的逻辑

        context?.let {
            // 可以在这里添加其他操作，如播放声音、启动 Activity 等
            /****跳转activity****/
            //val intent = Intent(this,alarm_open::class.java)
            context?.let{ctx->
                var timeInMillis = System.currentTimeMillis()
                Log.d("AlarmService","广播接收器的时间为${timeInMillis}")
                /*
                val alarm_isVibration = intent?.getBooleanExtra("alarm_isVibration",false)
                //Log.d("AlarmService","传递给广播接收器的isVibration为${alarm_isVibration}")
                val volume = intent?.getIntExtra("alarm_volume", 30)
                //Log.d("AlarmService","传递给广播接收器的isVibration为${volume}")
                val alarm_bell = intent?.getIntExtra("alarm_bell",0)
                //Log.d("AlarmService","传递给广播接收器的bell为${alarm_bell}")

                val alarm_isAwake = intent?.getBooleanExtra("alarm_isAwake",false)
                val alarm_awakeTime = intent?.getIntExtra("alarm_awakeTime",0)*/
                    //开启activity
                    val activityIntent = Intent(ctx, alarm_open::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 在非 Activity 上下文中启动
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 清除栈顶，避免重复堆叠
                        /*
                        putExtra("alarm_isVibration", alarm_isVibration) // 传递时间
                        putExtra("alarm_volume", volume) // 传递音量
                        putExtra("alarm_bell",alarm_bell)//传递铃声*/
                    }

                    // 在锁屏时显示 Activity，需要确保屏幕唤醒
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        // Android 8.1 及以上，解锁屏幕并显示 Activity
                        val keyguardManager = ctx.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if (keyguardManager.isKeyguardLocked) {
                            val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
                            val wakeLock = powerManager.newWakeLock(
                                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                "AlarmReceiver::WakeLock"
                            )
                            wakeLock.acquire(10 * 1000L) // 唤醒屏幕 10 秒
                        }
                    }

                    //启动
                    ctx.startActivity(activityIntent)
                    Log.d("AlarmService","准确启动")

            }
        }?: run {
            Log.e("AlarmReceiver", "Context is null")
        }
    }

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
            Toast.makeText(context, "调用 Python 处理失败（发送数据到python端）", Toast.LENGTH_SHORT).show()  // 使用context
            return false
        }
    }


}