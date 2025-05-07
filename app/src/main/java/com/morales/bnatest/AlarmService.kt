package com.morales.bnatest

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.util.*
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.morales.bnatest.SensorForegroundService.Companion.ACTION_SENSOR_DATA

class AlarmService:Service (){

    private  var alarmConfig:data_AlarmConfig = data_AlarmConfig(false,0,0, emptySet(),
        false,0,0,false,"10分钟")

    //轻唤醒提前的分钟
    private  var pre_awakeMin:Int = 0

    companion object{
        private const val TAG = "AlarmService"
        private const val ALARM_REQUEST_CODE = 100
        private const val ALARM_AWAKE_REQUEST_CODE = 101
        const val ALARM_ACTION = "com.example.ALARM_ACTION"
    }

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private val snoozeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 提取广播中的延迟时间
            val minnutesLaterMillis = intent?.getLongExtra("minutesLaterMills", 0) ?: 0
            Log.d("AlarmService", "Received snooze time: $minnutesLaterMillis")
            if (minnutesLaterMillis > 0) {
                Log.d("AlarmService", "Received snooze time: $minnutesLaterMillis")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    minnutesLaterMillis,
                    pendingIntent
                )
            }
        }
    }

    private val alarmCancelReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?)  {
            Log.d("AlarmPending","接收broadcast成功")
            cancelAlarm()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        // 初始化 alarmConfig
        alarmConfig = data_AlarmConfig_init(alarmConfig)
        Log.d("weekDay","数据类一系列的设置${alarmConfig}")
        val filter = IntentFilter("com.example.snooze")
        registerReceiver(snoozeReceiver, filter, RECEIVER_NOT_EXPORTED)

        val filter_alarmCancel = IntentFilter("com.example.alarmCancel")
        registerReceiver(alarmCancelReceiver,filter_alarmCancel, RECEIVER_NOT_EXPORTED)

    }

    override fun onBind(intent: Intent?): IBinder? {
            return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"服务开启")

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var intent:Intent? = null



        val weekday = getDayOfWeek(alarmConfig.timePicker_h,alarmConfig.timePicker_minute)
        Log.d("AlarmService","如果闹钟开启，那么日期是${weekday}")

        if(alarmConfig.isOpen == true && weekday in alarmConfig.weekdays){
            Log.d("AlarmService","闹钟会开启")
            setupAlarm(this)

        }else{
            Log.d("AlarmService","闹钟不会开启,此时闹钟开启${alarmConfig.isOpen}")
            cancelAlarm()
        }


        return START_STICKY // 服务被杀死后尝试重启
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销广播接收器
        unregisterReceiver(snoozeReceiver)
    }

    private fun extractMinutes(input: String): Int? {
        val matchResult = Regex("""\d+""").find(input)
        return matchResult?.value?.toIntOrNull()
    }

    /**
     * function:开启起床服务
     * @param：上下文
     * return：无
     * */
    private fun setupAlarm(context:Context){

        val intent:Intent
        if(alarmConfig.isAwake == true){
                intent = Intent(this, AlarmAwakeReceiver::class.java).apply {
                action = "com.morales.ACTION_ALARM_AWAKE"
                // 放其他 putExtra...
            }
        }else{
              intent = Intent(this, AlarmReceiver::class.java).apply {
                action = "com.morales.ACTION_ALARM_NORMAL"
                // 放其他 putExtra...
            }
        }


        val requestCode = if (alarmConfig.isAwake) {
            ALARM_AWAKE_REQUEST_CODE
        } else {
            ALARM_REQUEST_CODE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
        )


        val currentTimeMillis = System.currentTimeMillis()//当前的时间戳
        var calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()//现在时间

            set(Calendar.HOUR_OF_DAY, alarmConfig.timePicker_h)
            set(Calendar.MINUTE,alarmConfig.timePicker_minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }


        //判定其轻唤醒是否开启
        if(alarmConfig.isAwake == true)
        {
            //将时间提前
            //获取相应的选择的值
            val timeAwake = alarmConfig.awakeTime
            val minutesValue = extractMinutes(timeAwake)
            if (minutesValue != null) {
                pre_awakeMin = minutesValue
            }
            //Log.d("AlarmService","提前的时间为${minutesValue}")
            //减去相应的时间
            calendar.timeInMillis = calendar.timeInMillis - 60 * 1000 * minutesValue!!
        }

        // 判断目标时间是否小于当前时间
        if (calendar.timeInMillis <= currentTimeMillis) {
            // 如果小于当前时间，推到下一天
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }


        //检查权限
        // 检查权限（Android 12+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "缺少精确闹钟权限，跳转到设置")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        // 测试代码：直接发送广播
        val testIntent = Intent(this, AlarmAwakeReceiver::class.java).apply {
            action = "com.morales.ACTION_ALARM_AWAKE"
        }
        sendBroadcast(testIntent)
        Log.d("AlarmService", "广播发送")




        alarmManager.setExactAndAllowWhileIdle(

            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        /*
        Log.d("AlarmService", "正在设置闹钟，receiver = ${intent.component?.className}")
        Log.d("AlarmService", "设置时间 = ${calendar.timeInMillis}")
        Log.d("AlarmService", "PendingIntent = $pendingIntent")
        Log.d(TAG, "闹钟首次触发时间: ${calendar.timeInMillis}")
        Log.d(TAG, "PendingIntent created: ${pendingIntent.hashCode()}")
        Log.d(TAG, "Intent details: ${intent.component?.className} | ${intent.action}")*/

        Log.d("AlarmPending", "PendingIntent = $pendingIntent")

    }



    /**
     * funtion:结束AlarmManager
     * */
    private fun cancelAlarm() {
        listOf(ALARM_REQUEST_CODE, ALARM_AWAKE_REQUEST_CODE).forEach { requestCode ->
            val intent = if (requestCode == ALARM_AWAKE_REQUEST_CODE) {
                Intent(this, AlarmAwakeReceiver::class.java).apply {
                    action = "com.morales.ACTION_ALARM_AWAKE"
                }
            } else {
                Intent(this, AlarmReceiver::class.java).apply {
                    action = "com.morales.ACTION_ALARM_NORMAL"
                }
            }

            val cancelPendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )

            alarmManager.cancel(cancelPendingIntent)
            Log.d("AlarmPending", "Cancelled alarm with requestCode=$requestCode")
        }
    }



    /**
     * function:获取闹钟即timePicker的时间是周几
     * @param：（1）timePicker_hour
     *         (2)timePicker_minute
     * @return:string
     * */
    private fun getDayOfWeek(hour: Int, minute: Int): String {
        // 获取当前时间
        val currentCalendar = Calendar.getInstance()
        val currentTimeMillis = System.currentTimeMillis()

        // 设置目标时间
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 判断目标时间是否小于当前时间
        if (targetCalendar.timeInMillis <= currentTimeMillis) {
            // 如果小于当前时间，推到下一天
            targetCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 获取目标时间的星期几（1 = 星期日，2 = 星期一，...，7 = 星期六）
        val dayOfWeek = targetCalendar.get(Calendar.DAY_OF_WEEK)

        // 转换为中文星期几
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> "未知" // 理论上不会发生
        }
    }

    /**
     * function：对alarmConfig进行初始化
     * @param：data class alarm_config
     * @return:data class alarm_config
     * */
    private fun data_AlarmConfig_init(alarm_config:data_AlarmConfig):data_AlarmConfig{
        val isOpen = getBooleanFromSharedPreferences(this,"alarm_config","isOpen")
        alarm_config.isOpen = isOpen
        val timePicker_h = getIntFromSharedPreferences(this,"alarm_config","timePicker_hour")
        alarm_config.timePicker_h = timePicker_h
        val timePicker_minute = getIntFromSharedPreferences(this,"alarm_config","timePicker_min")
        alarm_config.timePicker_minute = timePicker_minute
        val weekdays = getSetStringFromSharedPreferences(this,"alarm_config","weekdays")
        alarm_config.weekdays = weekdays
        val isVibration =getBooleanFromSharedPreferences(this,"alarm_config","isVibration")
        alarm_config.isVibration = isVibration
        val bell = getIntFromSharedPreferences(this,"alarm_config","bell")
        alarm_config.bell = bell
        val volume = getIntFromSharedPreferences(this,"alarm_config","volume")
        alarm_config.volume = volume
        val isAwake =getBooleanFromSharedPreferences(this,"alarm_config","isAwake")
        alarm_config.isAwake =isAwake
        val awakeTime = getStringFromSharedPreferences(this,"alarm_config","awakeTime")
        alarm_config.awakeTime = awakeTime
        return alarm_config
    }

    //一系列function去能够初始化alarmConfig对象
    // 查找 Int 类型的值
    private fun getIntFromSharedPreferences(context: Context, fileName: String, key: String): Int {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getInt(key, 0)
        } else {
            0
        }
    }

    private fun getStringFromSharedPreferences(context: Context, fileName: String, key: String): String {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val value = sharedPreferences.getString(key, "0") ?: "0"
        return if (value.isEmpty()) "0" else value
    }

    private fun getBooleanFromSharedPreferences(context: Context, fileName: String, key: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getBoolean(key, false)
        } else {
            false
        }
    }

    private fun getSetStringFromSharedPreferences(context: Context, fileName: String, key: String): Set<String> {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    }
}