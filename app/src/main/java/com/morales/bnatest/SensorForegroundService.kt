package com.morales.bnatest


import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.morales.bnatest.MainActivity

class SensorForegroundService:Service(),SensorEventListener {

    private lateinit var sensorManager: SensorManager  //传感器管理器
    private var accelerometer :Sensor? = null          //加速度传感器

    /*Gyroscope*/
    private  var gyroscope:Sensor? = null

    private lateinit var mWakeLock: PowerManager.WakeLock //电源锁

    private var accelerometerThread: HandlerThread? = null
    private var gyroscopeThread: HandlerThread? = null
    private var accelerometerHandler: Handler? = null
    private var gyroscopeHandler: Handler? = null
    private var sensorsThread:HandlerThread? =null
    private var sensorsHandler:Handler? = null

    private var gyroscopeRollThread:HandlerThread? = null
    private var gyroscopeRollHandler:Handler? = null



    //companion定义一个伴生对象，可以被类调用，但是不需要创建类的实例
    companion object {
        private const val CHANNEL_ID = "SensorServiceChannel" //渠道ID
        private const val NOTIFICATION_ID = 1//通知ID

        const val ACTION_SENSOR_DATA = "com.morales.bnatest.ACTION_SENSOR_DATA"

        //定义三个广播传输过程中的键
        const val EXTRA_X = "extra_x"
        const val EXTRA_Y = "extra_y"
        const val EXTRA_Z = "extra_z"

        /*Gyroscope*/
        const val EXTRA_GX = "extra_x"
        const val EXTRA_GY = "extra_y"
        const val EXTRA_GZ = "extra_z"



    }

    //点击之后只从的操作
    override fun onCreate(){
        super.onCreate()

        Log.d("SensorService", "Service onCreate called")

        //获取传感器——加速度传感器
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometerThread = HandlerThread("AccelerometerThread").apply{
            start()
            accelerometerHandler = Handler(looper)
        }

        //获取传感器——陀螺仪
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        gyroscopeThread = HandlerThread("Gyroscope").apply{
            start()
            gyroscopeHandler = Handler(looper)
        }

        //获取传感器数据处理线程
        sensorsThread =HandlerThread("sensors").apply{
            start()
            sensorsHandler = Handler(looper)
        }

        gyroscopeRollThread = HandlerThread("gyroscopeRoll").apply{
            start()
            gyroscopeRollHandler = Handler(looper)
        }

        //获取PowerManager服务
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        // 创建并获取 WakeLock
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.morales.bnatest:SensorWakeLock")
        mWakeLock.acquire()


        //创建通知渠道
        createNotificationChannel()

        //创建通知对象
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        Log.d("startForeground","通知启动成功")


    }

    /**
     * 只要服务重新启动就去执行，开始服务
     * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accelerometer?.let {

            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        /*Gyroscope*/
        gyroscope?.let {
            val samplingPeriodUs = 20000
            sensorManager.registerListener(this, it, samplingPeriodUs)
        }

        return START_STICKY
    }

    /**
     * function:对传感器改变时逻辑进行复写
     * @param:传感器事件
     * @return：无
     * */
    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                // 在加速度计线程中处理数据
                accelerometerHandler?.post {
                    // 调用发送到 Python 端的方法
                    sendAccelerometerDataToPython(ax, ay, az)

                    // 发送广播
                    val intent = Intent(ACTION_SENSOR_DATA).apply {
                        putExtra(EXTRA_X, ax)
                        putExtra(EXTRA_Y, ay)
                        putExtra(EXTRA_Z, az)
                    }
                    sendBroadcast(intent)
                }

                /*
                //写一个显示广播，以方便给轻唤醒接收器对这些数据的获取
                val explicitIntent = Intent(this@SensorForegroundService, AlarmAwakeReceiver::class.java).apply {
                    action = ACTION_SENSOR_DATA
                    putExtra(EXTRA_X, x)
                    putExtra(EXTRA_Y, y)
                    putExtra(EXTRA_Z, z)
                }
                sendBroadcast(explicitIntent)*/

            }

            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]

                // 在陀螺仪线程中处理数据
                gyroscopeHandler?.post {
                    // 调用发送到 Python 端的方法
                    sendGyroscpeDataToPython(gx, gy, gz)


                    gyroscopeRollHandler?.post{
                        rollDectectionToPython(gx,gy,gz)
                    }

                    // 发送广播
                    val intent = Intent(ACTION_SENSOR_DATA).apply {
                        putExtra(EXTRA_GX, gx)
                        putExtra(EXTRA_GY, gy)
                        putExtra(EXTRA_GZ, gz)
                    }
                    sendBroadcast(intent)
                }
            }
        }


        /***这段代码记录***/


        /*
       // Log.d("sensor","加速度计数据: ax=$ax, ay=$ay, az=$az,陀螺仪数据: gx=$gx, gy=$gy, gz=$gz")
        if (ax != null && ay != null && az != null && gx != null && gy != null && gz != null) {
            sensorsHandler?.post{
                sendDataOfSensorsToPython(ax!!, ay!!, az!!, gx!!, gy!!, gz!!)
            }
        }*/



      // Log.d("sensor","加速度计数据: ax=$ax, ay=$ay, az=$az,陀螺仪数据: gx=$gx, gy=$gy, gz=$gz")
        //每次只能处理一种类型的数据
        //构建中间变量以保存

    }

    /**
     * 不对此函数进行复写
     * */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 传感器精度变化时的处理，这里可以不做处理
    }

    /**
     * function:活动销毁
     * */

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        // 释放 WakeLock
        if (mWakeLock.isHeld) {
            mWakeLock.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    /**
     * function:创建通知渠道并发送通知
     * */
    private fun createNotificationChannel() {
        // // 检查设备的Android版本是否为8.0（API级别26）或更高
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //创建通知渠道（notification通知）
            // CHANNEL_ID: 通知渠道的唯一标识符
            // "Sensor Service Channel": 通知渠道的名称，用户可以看到
            // NotificationManager.IMPORTANCE_DEFAULT: 通知的重要程度，默认级别
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sensor Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            //获取系统的通知管理器服务
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    /**
     * 创建并返回一个通知对象，用于启动前台服务
     */
    private fun createNotification(): Notification {
        // 创建一个Intent，用于在用户点击通知时打开MainActivity
        val notificationIntent = Intent(this, MainActivity::class.java)

        // 创建一个PendingIntent，作为通知的点击意图
        // 如果Android版本是6.0（API级别23）或更高，则使用FLAG_UPDATE_CURRENT和FLAG_IMMUTABLE标志
        // 否则仅使用FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )


        // 根据Android版本选择不同的通知构建方式
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("加速度计数据采集服务")
                .setContentText("正在采集加速度计数据")
                .setContentIntent(pendingIntent)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("加速度计数据采集服务")
                .setContentText("正在采集加速度计数据")
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    /**
     * function:将数据发送到python进行处理
     * */
    private fun sendAccelerometerDataToPython(x: Float, y: Float, z: Float) {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()
            val pyModule = python.getModule("accelerometerDataRecord")
            pyModule.callAttr("add_and_save_accelerometer_data", x.toDouble(), y.toDouble(), z.toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "调用 Python 处理失败（发送数据到python端）", Toast.LENGTH_SHORT).show()
        }
    }

    private  fun sendGyroscpeDataToPython(x: Float, y: Float, z: Float) {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()//得到运行环境
            val pyModule = python.getModule("gyroscope_collectData")
            pyModule.callAttr("saveDataToCsv", x.toDouble(), y.toDouble(), z.toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "调用 Python 处理失败", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * funtion:调用python代码处理翻身数据
     * */

    private fun rollDectectionToPython(x: Float, y: Float, z: Float){

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()//得到运行环境
            val pyModule = python.getModule("RollDection")
            val result = pyModule.callAttr("dectionRoll", x.toDouble(), y.toDouble(), z.toDouble())

            if (result != null){
                /*
                val timeRoll = result.asList()
                for(time in timeRoll){
                    Log.d("Roll","翻身时间为${time}")
                }*/

                val peakList = result.asList().map{item ->
                    val tuple = item.asList()
                    //Log.d("RollDection", "时间: tuple: ${tuple}")
                    val time = tuple[0].toString()
                    val strength = tuple[1].toDouble()
                    time to strength  // Kotlin 的 Pair
                }

                for ((time, strength) in peakList) {
                    Log.d("RollDection", "时间: $time，强度: $strength")
                    Log.d("RollDection","timestamp为${time::class.java},strength为${strength::class.java}")
                    saveTimeAndIntentsityToPrefs(this@SensorForegroundService,time,strength)
                    Log.d("RollDection","保存成功")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "预测翻身，调用 Python 处理失败", Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * function:将数据发送给python去保存
     * param:(1-3)accelerometer XYZ
     *       (4-6)gyroscope XYZ
     * return:null
     * */

    private fun sendDataOfSensorsToPython(ax:Float,ay:Float,az:Float,gx:Float,gy:Float,gz:Float){
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()//得到运行环境
            val pyModule = python.getModule("sensorAccAndGy")
            pyModule.callAttr("saveSensorDataToCSV", ax.toDouble(), ay.toDouble(), az.toDouble(),gx.toDouble(), gy.toDouble(), gz.toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "调用 Python 处理失败sensor", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * function:对时间和强度进行保存——保存方式SharedPreferences
     * @param:(1)时间戳
     *        (2)翻身强度
     * @return:无
     * */

    /**
     * function:对闹钟配置文件进行修改
     * @param:(1)文本 （2）key （3）value
     */
    private fun saveTimeAndIntentsityToPrefs(context: Context,  key: String, value: Any) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("StatusOfRollPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            is Double -> editor.putFloat(key, value.toFloat())
            else -> throw IllegalArgumentException("不支持的数据类型: ${value.javaClass.name}")
        }

        editor.apply()
        Log.d("RollDection","保存成功")
    }


}