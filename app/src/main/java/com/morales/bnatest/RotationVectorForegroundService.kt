package com.morales.bnatest

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class RotationVectorForegroundService:Service(),SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private var sensorsThread: HandlerThread? = null
    private var sensorsHandler: Handler? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SensorServiceChannel_rotationVector"
        private const val TAG = "RotationVector"
    }

    override fun onCreate() {
        super.onCreate()
        //初始化传感器管理器
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        //init HandlerThread
        sensorsThread = HandlerThread("RotationSensorThread").apply{
            start()
            sensorsHandler = Handler(looper)//创建一个 Handler 对象 sensorsHandler，并把 HandlerThread 的 Looper 传递给它
        }
        Log.d(TAG,"send ready")
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG,"send successfully")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 注册旋转向量传感器
        rotationSensor?.let {
            sensorManager.registerListener(this, it,SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "旋转向量传感器已注册")
        } ?: run {
            Log.e(TAG, "旋转向量传感器不可用")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            // 获取旋转向量数据（四元数）
            val x = event.values[0] // 四元数的 x 分量
            val y = event.values[1] // 四元数的 y 分量
            val z = event.values[2] // 四元数的 z 分量
            val w = event.values.getOrNull(3) ?: 0f // 四元数的 w 分量（标量）
            val accuracy = event.values.getOrNull(4) ?: -1f // 精度（可选）

            // 在传感器处理线程中打印日志
            sensorsHandler?.post {
                //Log.d(TAG, "旋转向量数据: x=$x, y=$y, z=$z, w=$w, accuracy=$accuracy")
                // 可选：将四元数转换为欧拉角（俯仰、横滚、偏航）以便更直观
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // 偏航
                val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat() // 俯仰
                val roll = Math.toDegrees(orientation[2].toDouble()).toFloat() // 横滚
                Log.d(TAG, "欧拉角: 偏航(azimuth)=$azimuth°, 俯仰(pitch)=$pitch°, 横滚(roll)=$roll°")

                //对数据进行保存
                sendDataOfRotationVectorToPython(azimuth,pitch,roll)
            }
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


    override fun onDestroy() {
        super.onDestroy()
        // 注销传感器监听
        sensorManager.unregisterListener(this)
        // 停止 HandlerThread
        sensorsThread?.quitSafely()
        Log.d(TAG, "服务销毁，传感器监听已注销")
    }

    private fun createNotification(): android.app.Notification {
        // 创建通知渠道（Android 8.0+ 要求）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Sensor Service Channel",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // 创建通知
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("传感器服务")
            .setContentText("正在监测旋转向量数据")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private  fun sendDataOfRotationVectorToPython(x: Float, y: Float, z: Float) {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()//得到运行环境
            val pyModule = python.getModule("rotationVectorData")
            pyModule.callAttr("saveRotationVectorDataToCSV", x.toDouble(), y.toDouble(), z.toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "调用 Python 处理失败", Toast.LENGTH_SHORT).show()
        }
    }


}