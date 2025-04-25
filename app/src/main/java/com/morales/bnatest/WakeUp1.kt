

package com.morales.bnatest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import org.tensorflow.lite.Interpreter
import com.morales.bnatest.databinding.FragmentHomeBinding
import java.util.concurrent.TimeUnit
import android.widget.Button
import com.morales.bnatest.SleepData // 导入 SleepData 类
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import java.io.File
import android.os.SystemClock
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import java.io.FileOutputStream
import kotlin.experimental.and
import android.os.PowerManager
import android.provider.Settings

class WakeUp1 : AppCompatActivity(){ //kotlin是可以多继承的
    //时间显示对象
    private lateinit var timeTextView: TextView
    //surfaceView对象，用于绘制波形
    private lateinit var surfaceView: SurfaceView
    //surfaceHolder对象，用于surfaceView画布
    private lateinit var surfaceHolder: SurfaceHolder
    //handler对象，用于在主线程中执行任务
    private val handler = Handler(Looper.getMainLooper())
    //定义一个常量，用于表示时间更新间隔，单位ms
    private val updateInterval = 1000L


    //录音对像
    private var audioRecord: AudioRecord? = null

    //录音参数
    private val sampleRate = 16000//采样率
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO//单声道
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT//编码格式，此处为16weiPCM编码
    private var isRecording = false//录音标志位

    //计时器部分
    private lateinit var binding: FragmentHomeBinding
    private var count = 0
    private lateinit var handler_count: Handler//用于主线程中执行计时器任务
    private lateinit var runnable: Runnable//用于实现计时器逻辑
    private lateinit var timerTextView: android.widget.TextView//用于显示计时器的时间
    //录音按钮
    private lateinit var recordButton: Button

    private lateinit var sleepData: SleepData //这是一个SleepData类用来接收

    private var snoreCount = 0
    private val oneMinuteMillis = 60 * 1000L
    private var startTime = SystemClock.elapsedRealtime()
    private var currentAudioData = mutableListOf<ShortArray>()

    private lateinit var snoringPredictTextView: TextView

    //acclerometer部分
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var accelerometerXValueTextView: TextView
    private lateinit var accelerometerYValueTextView: TextView
    private lateinit var accelerometerZValueTextView: TextView

    private lateinit var gyroscopeXValueTextView: TextView
    private lateinit var gyroscopeYValueTextView: TextView
    private lateinit var gyroscopeZValueTextView: TextView

    //创建一个WakeLock对象
    private lateinit var mWakeLock:PowerManager.WakeLock

    private var waveformData = mutableListOf<Short>()

    private lateinit var finishReceiver: BroadcastReceiver



    /**
     * function:用于更新时间显示
     * */
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()//在此调用时间更新方法
            handler.postDelayed(this, updateInterval)
        }
    }

    /**
     * function：定义一个名为 requestPermissionLauncher 的 ActivityResultLauncher 对象，用于请求录音权限。
     * */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startRecording()//开启录音
        } else {
            Toast.makeText(this, "录音权限被拒绝，无法启动录音", Toast.LENGTH_SHORT).show()
        }
    }


    //Interpreter 对象，用于加载和运行 TensorFlow Lite 模型
    private lateinit var tflite: Interpreter
    //audioDataQueue 的 BlockingQueue 对象，用于存储音频数据
    private val audioDataQueue: BlockingQueue<ShortArray> = LinkedBlockingQueue()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wake_up1)// 设置活动的布局文件

        timerTextView=findViewById(R.id.Timer)//初始化 timerTextView 对象。
        timeTextView = findViewById(R.id.wakeup_clock)// 初始化 timeTextView 对象。
        surfaceView = findViewById(R.id.SurfaceView)//初始化 surfaceView 对象
        surfaceHolder = surfaceView.holder //初始化 surfaceHolder 对象

        // 初始化 TextView
        snoringPredictTextView = findViewById(R.id.SnoringPredict)



        // 获取传递过来的 SleepData 对象
        sleepData = intent.getParcelableExtra<SleepData>("sleepData")!!

        //初始化accelerometer部分
        accelerometerXValueTextView = findViewById(R.id.accelerometer_x_value)
        accelerometerYValueTextView = findViewById(R.id.accelerometer_y_value)
        accelerometerZValueTextView = findViewById(R.id.accelerometer_z_value)

        //初始化gyroscope部分
        gyroscopeXValueTextView = findViewById(R.id.gyroscope_x_value)
        gyroscopeYValueTextView = findViewById(R.id.gyroscope_y_value)
        gyroscopeZValueTextView = findViewById(R.id.gyroscope_z_value)

        //获取传感器服务
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)



        startForegroundService()

        // 注册广播接收器
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.ACTION_FINISH_WAKEUP") {
                    finish()
                    Log.d("AlarmSerice", "收到结束广播，已经关闭Activity")
                }
            }
        }
        val filter = IntentFilter("com.example.ACTION_FINISH_WAKEUP")
        registerReceiver(finishReceiver, filter)


        /*
        //WakeLock初始化
        // PowerManager.PARTIAL_WAKE_LOCK 选项为 CPU 和所有硬件会一直工作
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,  "com.morales.bnatest:MyTag" )
        mWakeLock.acquire()*/
        //设置长按监听器

        recordButton=findViewById(R.id.ButtonRecord)

        recordButton.setOnClickListener {
            // 停止计时
            stopTimer()
            //获取结束时间
            val endTime = getCurrentTime()
            //更新SleepData对象的endTime属性
            sleepData = sleepData.copy(endTime = endTime)


            //获得结束日期
            // 获取当前日期并更新 sleepData 的 endDate 属性
            val endDate = getCurrentDate()
            sleepData = sleepData.copy(endDate = endDate)

            //计算睡眠时间
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val startDate = timeFormat.parse(sleepData.startTime)
                val endDate = timeFormat.parse(sleepData.endTime)
                val durationMillis = endDate.time - startDate.time
                val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
                val sleepDurationStr = String.format("%02d:%02d", hours, minutes)
                sleepData = sleepData.copy(sleepDuration = sleepDurationStr)
            } catch (e: Exception) {
                e.printStackTrace()
                sleepData = sleepData.copy(sleepDuration = "计算失败")
            }

            //计算鼾声时间
            val totalCountStr: String = totalCount.toString()
            sleepData=sleepData.copy(snoringDuration = totalCountStr )

            // 日志查看属性信息
            Log.d("WakeUp1", "日期: ${sleepData.date}")
            Log.d("WakeUp1", "开始时间: ${sleepData.startTime}")
            Log.d("WakeUp1", "结束时间: ${sleepData.endTime}")
            Log.d("WakeUp1","创建文件路径：${sleepData.snoreDataPath}")
            Log.d("WakeUp1", "睡眠时间: ${sleepData.sleepDuration}")
            Log.d("WakeUp1","结束日期：${sleepData.endDate}")
            Log.d("WakeUp1","鼾声持续时间：${sleepData.snoringDuration}")

            //保存SleepData到SharedPerferences
            saveSleepDataToSharedPreferences(sleepData)
            Log.d("DataSave","保存成功")

            //将更新后的SleepData对象返回给调用者（Intent通信）
            val resultIntent = Intent()
            resultIntent.putExtra("updatedSleepData", sleepData)
            setResult(android.app.Activity.RESULT_OK, resultIntent)

            //调动其python文件，拿到json
            getWakeUpAndDeepSleep()

            
            stopService(Intent(this,AlarmService::class.java))
            stopService(Intent(this, SensorForegroundService::class.java))
            // 关闭当前活动，返回上一个界面
            finish()
            true // 返回 true 表示消费了该长按事件



        }


        // 将 updateTimeRunnable 添加到 handler 中，开始更新时间显示
        handler.post(updateTimeRunnable)

        // 判断是否授予了录音权限。
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startRecording()
            startTimer()

            //注册广播接收器
            /*
            val intentFilter = IntentFilter(SensorForegroundService.ACTION_SENSOR_DATA)
            registerReceiver(sensorDataReceiver, intentFilter)*/

        }

        try {
            Log.d("ModelLoading", "Starting to load model from assets")
            val inputStream = assets.open("SonrinModel.tflite")
            Log.d("ModelLoading", "open ok")

            // 获取模型的字节数组
            val modelData = ByteArray(inputStream.available())
            // 读取文件内容到字节数组中
            inputStream.read(modelData)
            // 关闭输入流
            inputStream.close()

            // 创建一个 ByteBuffer 对象，用于存储模型数据
            val modelBuffer = ByteBuffer.allocateDirect(modelData.size).order(ByteOrder.nativeOrder())
            // 将模型数据写入 ByteBuffer 中
            modelBuffer.put(modelData)
            // 将 ByteBuffer 的位置重置为 0。
            modelBuffer.rewind()

            // 使用 ByteBuffer 初始化 tflite 对象。
            tflite = Interpreter(modelBuffer)
            Log.d("ModelLoading", "Model loaded successfully")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "加载模型失败", Toast.LENGTH_SHORT).show()
            Log.e("ModelLoading", "Error loading model: ${e.message}")
        }

        // 启动推理线程
        startInferenceThread()
    }

    /*

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == SensorForegroundService.ACTION_SENSOR_DATA) {
                val x = intent.getFloatExtra(SensorForegroundService.EXTRA_X, 0f)
                val y = intent.getFloatExtra(SensorForegroundService.EXTRA_Y, 0f)
                val z = intent.getFloatExtra(SensorForegroundService.EXTRA_Z, 0f)

                val gx = intent.getFloatExtra(SensorForegroundService.EXTRA_GX, 0f)
                val gy = intent.getFloatExtra(SensorForegroundService.EXTRA_GY, 0f)
                val gz = intent.getFloatExtra(SensorForegroundService.EXTRA_GZ, 0f)

                // 更新 UI
                accelerometerXValueTextView.text = "%.2f".format(x)
                accelerometerYValueTextView.text = "%.2f".format(y)
                accelerometerZValueTextView.text = "%.2f".format(z)

                gyroscopeXValueTextView.text = "%.2f".format(gx)
                gyroscopeYValueTextView.text = "%.2f".format(gy)
                gyroscopeZValueTextView.text = "%.2f".format(gz)
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        stopRecording()
        handler.removeCallbacksAndMessages(null)

        stopService(Intent(this, SensorForegroundService::class.java))
        stopService(Intent(this,AlarmService::class.java))


        // 注销广播接收器
        //unregisterReceiver(sensorDataReceiver)
        unregisterReceiver(finishReceiver)
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



    /**
     * function:用于时间更新
     * */
    private fun updateTime() {
        try {
            //获取当前时间
            val currentDate = Date()
            //创建格式化日期对此昂
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            //格式化当前日期
            val formattedDate = dateFormat.format(currentDate)
            timeTextView.text = formattedDate
        } catch (e: Exception) {
            e.printStackTrace()
            timeTextView.text = "时间显示出错"
        }
    }

    /**
     * function:开始录音
     * */
    private fun startRecording() {
        try {
            //计算音频录制所需的最小缓冲区大小
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
           // 检查是否已授予录音权限
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }


            //创建当前日期的文件夹
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateFolderName = dateFormat.format(Date())
            /*
            *创建文件
            * filesDir：应用的内部存储目录
            * dataFolderName文件名称
            * */
            val dateFolder = File(filesDir, dateFolderName)
            if (!dateFolder.exists()) {
                dateFolder.mkdirs()
            }

            sleepData=sleepData.copy(snoreDataPath=dateFolder.absolutePath)

            // 初始化 AudioRecord 对象，用于从麦克风录制音频
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            isRecording = true
            //设置录音状态为 true，表示开始录音
            audioRecord?.startRecording()

            // 启动录音线程
            Thread {
                //创建一个缓冲区，用于存储音频数据
                val buffer = ShortArray(sampleRate)
                // 当录音状态为 true 时，持续录制音频
                while (isRecording) {
                    // // 从 AudioRecord 中读取音频数据到缓冲区
                    val readSize = audioRecord?.read(buffer, 0, sampleRate) ?: 0
                    // 如果读取的数据大小等于采样率，表示成功读取了一个完整的音频帧
                    if (readSize == sampleRate) {
                        // 复制音频数据，避免原始缓冲区被修改
                        val audioData = buffer.clone()
                        // 将音频数据放入队列，供推理线程处理
                        audioDataQueue.put(audioData)

                        val audioDataStr = audioData.joinToString(",")
                        Log.d("AudioData", "读取到的音频数据: $audioDataStr")

                        // 发送绘图任务到主线程
                        handler.post {
                            drawWaveform(audioData)//绘图线程
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "启动录音失败，请检查设备或权限", Toast.LENGTH_SHORT).show()
        }
    }

    private var totalCount:Int=0


    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    /**
     * 用于启动推理线程
     * */
    private fun startInferenceThread() {
        Thread {
            while (true) {
                val audioData = audioDataQueue.take()
                val result = predictSnoring(audioData)
                var value_min = 0
                Log.d("SnoringDetection", "Snoring result: $result")

                /*保存鼾声部分*/
                currentAudioData.add(audioData)
                //统计snore次数
                if (result == 1) {
                    totalCount++
                    snoreCount++
                }
                //检查是否过了一分钟
                val currentTime = SystemClock.elapsedRealtime()

                if (currentTime - startTime >= oneMinuteMillis) {
                    // 检查 snore 次数是否达到 5 次或以上
                    if (snoreCount >= 1) {
                        saveAudioData(currentAudioData)
                        value_min = 1
                    }
                   var currentMinuteKey = getCurrentMinuteKey()
                    // 保存到 SharedPreferences
                    saveMinuteSnoringData(currentMinuteKey, value_min)
                    Log.d("saveRecord","$currentMinuteKey 数据保存成功")

                    // 重置计数器和时间
                    snoreCount = 0
                    startTime = currentTime
                    currentAudioData.clear()
                    // 初始化 TextView
                    snoringPredictTextView = findViewById(R.id.SnoringPredict)
                }


            }
        }.start()

    }

    /**
     *fuction： 用于对音频数据进行推理
     * @param:buffer对象
     * */
    private fun predictSnoring(buffer: ShortArray): Int {
        val inputBuffer = ByteBuffer.allocateDirect(sampleRate * 4).order(ByteOrder.nativeOrder())
        for (value in buffer) {
            inputBuffer.putFloat(value / 32768.0f)
        }

        val output = Array(1) { FloatArray(1) }
        tflite.run(inputBuffer, output)

        val threshold = 0.3f

       // val result=output[0][0]
        //return if (output[0][0] > threshold) 1 else 0
        val result = if (output[0][0] > threshold) 1 else 0


        // 更新 TextView 的值
        runOnUiThread {
            snoringPredictTextView.text = result.toString()
        }
            return result

    }

    /**
     * 这部分有问题，不是白色没有问题，而是白色没有看到问题
     * */

    private fun drawWaveform(buffer: ShortArray) {
        try {

            val bufferSize = buffer.size
            Log.d("buffer11","缓冲区的大小为${bufferSize}")
            if (waveformData.isEmpty()) {
                waveformData = MutableList(12 * bufferSize) { 0 }
            }

            //更新列表
            for (i in 0 until (11 * bufferSize)) {
                waveformData[i] = waveformData[i + bufferSize]
            }

            for (i in 0 until bufferSize) {
                waveformData[i + (11 * bufferSize)] = buffer[i]
            }
            val canvas = surfaceHolder.lockCanvas()

            if (canvas != null) {
                // 使用指定颜色填充整个画布
                canvas.drawColor(Color.WHITE)
                val paint = Paint()
                paint.color = Color.BLUE
                val width = canvas.width
                val height = canvas.height
                val step = width / (12 * bufferSize).toFloat()

                for (i in 0 until (12 * bufferSize - 1)) {
                    val x1 = i * step
                    val y1 =
                        (height / 2) + (waveformData[i].toFloat() / Short.MAX_VALUE) * (height / 2)
                    val x2 = (i + 1) * step
                    val y2 =
                        (height / 2) + (waveformData[i + 1].toFloat() / Short.MAX_VALUE) * (height / 2)
                    canvas.drawLine(x1, y1, x2, y2, paint)
                }
                surfaceHolder.unlockCanvasAndPost(canvas)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "绘制波形图失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * function:展示计时器的时间
     * */
    private fun startTimer() {
        count = 0
        handler_count = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {

                count++
                val str = showTimeCount(count.toLong())
                timerTextView.text = str
                handler.postDelayed(this, 1000) // 每一秒刷新一次
            }
        }
        runnable.run()
    }

    /**
     * function:展示时间
     * @param：一个计数器
     * @return：时：分：秒
     * */
    private fun showTimeCount(count: Long): String {
        val hours = TimeUnit.SECONDS.toHours(count)
        val minutes = TimeUnit.SECONDS.toMinutes(count) % 60
        val seconds = count % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * function:停止计数
     * */

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
    }

    /**
     * function:获取当前时间
     * */
    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }

    /**
     * function:获得当前的日期
     * */
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    /**
     * function:保存音频数据到文件
     * @param: audioDataList 音频数据列表，每个元素是一个 ShortArray，表示一段音频数据
     * */
    private fun saveAudioData(audioDataList: List<ShortArray>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        //根据当前时间生成文件名，并添加 ".pcm" 后缀，表示这是一个 PCM 音频文件
        val fileName = "${dateFormat.format(Date())}.pcm"
        val dateFolderName = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dateFolder = File(filesDir, dateFolderName)
        // 创建一个 File 对象，表示要保存的音频文件路径。
        val file = File(dateFolder, fileName)

        try {
            // 打开一个 FileOutputStream，用于写入音频数据到文件
            val outputStream = FileOutputStream(file)
            // 遍历音频数据列表
            for (audioData in audioDataList) {
                // 将 ShortArray 转换为 ByteArray。这通常是因为 FileOutputStream 只能写入字节数组。
                val byteArray = ShortArrayToByteArray(audioData)
                outputStream.write(byteArray)
            }
            outputStream.close()
            Log.d("AudioSaving", "Audio data saved to ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("AudioSaving", "Error saving audio data: ${e.message}")
        }
    }

    private fun ShortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteArray = ByteArray(shortArray.size * 2)
        for (i in shortArray.indices) {
            val shortValue = shortArray[i].toInt() // 将 Short 转换为 Int
            byteArray[i * 2] = (shortValue and 0xFF).toByte()
            byteArray[i * 2 + 1] = ((shortValue shr 8) and 0xFF).toByte()
        }
        return byteArray
    }

    /**
     * function:一个名为 saveSleepDataToSharedPreferences 的私有函数
     * @param:接收一个sleepData作为参数
     * */
    private fun saveSleepDataToSharedPreferences(sleepData: SleepData) {
        /*
        * 使用 getSharedPreferences 方法获取一个 SharedPreferences 实例。
        * "SleepDataPrefs" 是存储数据的文件名，表示所有与睡眠数据相关的键值对将存储在这个文件中。
        * Context.MODE_PRIVATE 指定该存储文件是私有的，只有当前应用可以访问。
        * */
        val sharedPreferences: SharedPreferences = getSharedPreferences("SleepDataPrefs", Context.MODE_PRIVATE)
        //调用 sharedPreferences.edit() 获取一个 SharedPreferences.Editor 对象，用于对存储的数据进行修改。
        val editor = sharedPreferences.edit()
        //使用 Gson 库将 SleepData 对象序列化为 JSON 字符串

        // 检查键是否已经存在，如果存在则移除
        if (sharedPreferences.contains(sleepData.date)) {
            editor.remove(sleepData.date)
        }


        val gson = Gson()
        val sleepDataJson = gson.toJson(sleepData)
        //将数据保存到 SharedPreferences
        editor.putString(sleepData.date, sleepDataJson)
        editor.apply()
        //Toast.makeText(this, "睡眠数据已保存", Toast.LENGTH_SHORT).show()
    }


    /**调试时所用*/

    // 保存每分钟的鼾声数据到 SharedPreferences
    private fun saveMinuteSnoringData(key: String, value: Int) {
        val sharedPreferences = getSharedPreferences("MinuteSnoringData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun getCurrentMinuteKey(): String {
        val calendar = Calendar.getInstance()
        // 修改日期格式化模式，包含日期和时间（时和分）
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }

    /**
     *function:保存得到深睡浅睡觉醒的信息——Json
     * @param：None
     * @return：None
     **/

    private  fun getWakeUpAndDeepSleep() {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()//得到运行环境
            val pyModule = python.getModule("sensor_data")
            val result=pyModule.callAttr("getWakeUpAndDeepsleepInterval")

            val jsonString =result.toString()

            saveJsonToSharedPreferences(jsonString)
            Log.d("WakeUpAndDeepSleep", "JSON 内容: $jsonString")
            Log.d("WakeUpAndDeepSleep","成功得到Json")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "调用 Python1 处理失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * function:保存深睡这方面的信息
     * @param：json、
     * @return:无
     * */

    private fun saveJsonToSharedPreferences(jsonString: String) {

        if(isSavedRecord(60)){

            // 获取当前日期作为键
            val currentDate = sleepData.date
            // 使用 SharedPreferences 保存 JSON 数据
            val sharedPreferences: SharedPreferences = getSharedPreferences("WakeUpAndDeepSleep", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // 检查键是否已经存在，如果存在则删除
            if (sharedPreferences.contains(currentDate)) {
                editor.remove(currentDate)
                Log.d("WakeUpAndDeepSleep", "已删除 SharedPreferences 中键为 $currentDate 的原有内容")
            }

            editor.putString(currentDate, jsonString)
            editor.apply()

            Log.d("WakeUpAndDeepSleep", "成功保存 Json 到 SharedPreferences，键: $currentDate")
        }

        else{

            Toast.makeText(this, "1个小时以内无法生成睡眠报告", Toast.LENGTH_SHORT).show()

        }

    }

    /**
     * function:判断加速度数据的长度，以此判断结束和开始之间的间隔，决定是否保存信息
     * @param：时间阈值（以分钟为单位）
     * @return：bool
     * */

    private fun isSavedRecord(minThreshold: Int): Boolean{

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()//得到运行环境
            val pyModule = python.getModule("sensor_data")
            val result=pyModule.callAttr("calculateLen").toInt()

            if(result > (minThreshold * 60) ){
                return true
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
            Toast.makeText(this, "调用 Python1 处理失败", Toast.LENGTH_SHORT).show()
        }

    }

}



