package com.morales.bnatest

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class alarm_open: AppCompatActivity() {

    // 声明 Vibrator 实例，用于控制设备振动
    private lateinit var mVibrator: Vibrator
    //音乐播放器
    private lateinit var mediaPlayer: MediaPlayer
    //音乐管理器
    private lateinit var audioManager: AudioManager
    //按键
    private lateinit var buttonOfEnd: Button
    private lateinit var buttonOfGoOff:Button
    //闹钟的时间间隔（分钟为单位）
    private val time_interval_alarm = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("AlarmService", "Activity onCreate called")

        // 设置锁屏显示和屏幕点亮
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
            Log.d("AlarmService", "设置锁屏和点亮标志成功")
        } catch (e: Exception) {
            Log.e("AlarmService", "设置锁屏标志失败: ${e.message}")
        }

        setContentView(R.layout.alarm_open)
        Log.d("AlarmService", "setContentView called")
        setContentView(R.layout.alarm_open)

        //获取button
        buttonOfEnd = findViewById(R.id.button_endSlessp)
        buttonOfGoOff = findViewById(R.id.button_goOffAgain)

        /*
        // 获取传递的参数
        val alarm_isVibration = intent.getBooleanExtra("alarm_isVibration", false)
        Log.d("AlarmService", "传递给最终的活动为的isVibration为${alarm_isVibration}")
        val alarm_volume = intent.getIntExtra("alarm_volume", 50)
        val alarm_bell = intent.getIntExtra("alarm_bell", 0)
        Log.d("AlarmService", "传递给最终的活动为的isVibration为${alarm_bell}")*/

        //从配置文件中获取内容
        // (1)获取震动——boolean
        val alarm_isVibration = getBooleanFromSharedPreferences(this,"alarm_config","isVibration")
        Log.d("AlarmService", "从配置文件中得到的isVibration为${alarm_isVibration}")
        val alarm_volume = getIntFromSharedPreferences(this,"alarm_config","volume")
        Log.d("AlarmService", "从配置文件中得到的volume为${alarm_volume}")
        val alarm_bell = getIntFromSharedPreferences(this,"alarm_config","bell")
        Log.d("AlarmService", "从配置文件中得到的bell为${alarm_bell}")


        if (alarm_isVibration == true) {
            // 从应用上下文中获取 Vibrator 系统服务
            mVibrator = getApplicationContext().getSystemService(Vibrator::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上版本使用 VibrationEffect
                //val vibrationEffect = VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)
                val pattern = longArrayOf(0, 1000) // 震动模式：0ms 延迟后震动 1000ms，重复
                val vibrationEffect = VibrationEffect.createWaveform(pattern, 0) // 0 表示从第0个元素开始无限重复
                if (mVibrator.hasVibrator()) {
                    mVibrator.vibrate(vibrationEffect)
                }
            } else {
                // Android 8.0 以下版本直接使用 Vibrator 的 vibrate 方法
                val pattern = longArrayOf(0, 1000) // 震动模式：0ms 延迟后震动 1000ms，重复
                if (mVibrator.hasVibrator()) {
                    mVibrator.vibrate(pattern, 0) // 0 表示无限重复
                }
            }

            Log.d("AlarmService", "开始这震动")
        }

        val music_list =
            listOf<Int>(R.raw.thedreamofsummer, R.raw.candy, R.raw.fullofenergy, R.raw.springday)

        /***********MusicPlayer***********/

        // 初始化 AudioManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // 初始化 MediaPlayer，加载 res/raw/alarm_sound.mp3
        mediaPlayer = MediaPlayer.create(this, music_list[alarm_bell])

        // 设置循环播放
        mediaPlayer.isLooping = true

        // 设置音量（例如设置为 50% 音量）
        setVolume(alarm_volume) // 0-100 的百分比

        // 开始播放
        mediaPlayer.start()


        //结束闹钟的长按监听器
        buttonOfEnd.setOnLongClickListener {
            // 停止振动
            if (::mVibrator.isInitialized) {
                mVibrator.cancel()
            }

            // 停止并释放 MediaPlayer
            if (::mediaPlayer.isInitialized) {
                try {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                    mediaPlayer.release()
                } catch (e: Exception) {
                    Log.e("AlarmService", "MediaPlayer 释放失败: ${e.message}")
                }
            }

            // 发送广播通知 WakeupActivity 结束
            val intent = Intent("com.example.ACTION_FINISH_WAKEUP")
            sendBroadcast(intent)
            Log.d("AlarmService", "已发送广播通知 WakeupActivity 结束")

            // 结束当前 Activity
            finish()

            true // 表示长按事件已处理
        }

        //闹钟再响的点击监听器
        buttonOfGoOff.setOnClickListener{
            //拿到对应分钟的值
            val currentTimeMillis = System.currentTimeMillis()
            val minutesLaterMillis = currentTimeMillis + time_interval_alarm * 60 * 1000
            //将这个值传递给AlarmService

            //注册广播
            val broadcastIntent = Intent("com.example.snooze").apply{
                putExtra("minutesLaterMills",minutesLaterMillis)
            }
            sendBroadcast(broadcastIntent)
            Log.d("AlarmService","闹钟再响的时间戳为${minutesLaterMillis}")

            // 停止振动
            if (::mVibrator.isInitialized) {
                mVibrator.cancel()
            }

            // 停止并释放 MediaPlayer
            if (::mediaPlayer.isInitialized) {
                try {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                    mediaPlayer.release()
                } catch (e: Exception) {
                    Log.e("AlarmService", "MediaPlayer 释放失败: ${e.message}")
                }
            }

            finish()

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保资源释放
        if (::mVibrator.isInitialized) {
            mVibrator.cancel()
        }

    }

    // 设置音量的方法（百分比 0-100）
    private fun setVolume(volumePercent: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) // 获取最大媒体音量
        val volume = (maxVolume * volumePercent) / 100 // 计算实际音量值
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, // 控制媒体音量
            volume,                    // 设置音量值
            0                          // 标志位，0 表示不显示系统音量 UI
        )
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
