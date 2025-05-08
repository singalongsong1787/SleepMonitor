package com.morales.bnatest.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morales.bnatest.R
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.morales.bnatest.SleepChartView
import com.morales.bnatest.SleepData
import com.morales.bnatest.WaveformView
import com.morales.bnatest.wakeUpAndDeepSleep
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

// 定义 SnoringAdapter 类，继承自 RecyclerView.Adapter，用于处理鼾声数据列表的显示
/**
 * 类名：SnoringAdapter-继承RecyeclerView适配器
 * 泛型：RecyclerView.ViewHolder 是一个通用的类，用于封装 RecyclerView 中每个列表项的视图。
 *     它的主要作用是缓存视图的引用，避免在 onBindViewHolder 方法中重复调用 findViewById，从而提高性能
 * 类成员：上下文，文件名，展示文件名、原始文件名，视图
 * */
class SnoringAdapter(
    private val context: Context, // 上下文对象，用于获取资源等操作
    private val fileNames: List<String> ,// 存储文件名的列表，每个文件名对应一个列表项
    private val displayFileNames: List<String>,//用于timerTextView的东西
    private val filePaths: List<String>,//用于存储路径读取文件时所用的
    private val snoringView: RecyclerView, // 添加 RecyclerView 参数
    private val specifiedDate: String? = null // 添加指定日期参数

) :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0 //headerView
    private val VIEW_TYPE_ITEM = 1   //itemView


    /*实现暂停功能*/
    private var playingPosition = -1 // 记录正在播放的项的位置，-1 表示没有正在播放的项
    //注释：显示播放和读取都要去建立一个类对象
    private var audioTrack: AudioTrack? = null // 用于播放音频的 AudioTrack 实例（？表示这个类型可以为空）
    private var inputStream: FileInputStream? = null // 用于读取音频文件的输入流
    @Volatile private var isPlaying = false
    //这个可以用在seekBar中
    private val handler = Handler(Looper.getMainLooper())//用于主线程中执行任务

    //创建播放线程
    private var playbackThread:Thread? = null

     /*
    // 用于创建新的 ViewHolder，ViewHolder 负责缓存列表项中的视图组件
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnoringViewHolder {
        Log.d("SnoringAdapter", "onCreateViewHolder called")
        // 使用 LayoutInflater 从布局文件 item_snoring.xml 中创建视图
        val view = LayoutInflater.from(context).inflate(R.layout.item_snoring, parent, false)
        // 返回一个新的 SnoringViewHolder 实例，传入创建的视图
        return SnoringViewHolder(view)
    }*/

    // 用于创建新的 ViewHolder，ViewHolder 负责缓存列表项中的视图组件（复写OnCreateViewHolder方法）
    /**
     * function:用于绑定视图
     * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //返回一个函数名称
        return when (viewType) {
            VIEW_TYPE_HEADER -> { //lambda函数调用，函数的高阶用法
                val view = LayoutInflater.from(context).inflate(R.layout.header_snoring, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_snoring, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    /*
    // 将数据绑定到 ViewHolder 上，用于显示特定位置的数据
    override fun onBindViewHolder(holder: SnoringViewHolder, position: Int) {
        Log.d("SnoringAdapter", "onBindViewHolder called for position: $position")
        if (position < fileNames.size) {
            // 从文件列表中获取指定位置的文件名
            val fileName = fileNames[position]
            Log.d("SnoringAdapter", "Binding file name: $fileName at position: $position")
            // 将文件名设置到 TextView 上，用于显示在界面上
            holder.snoringTime.text = fileName
        } else {
            Log.e("SnoringAdapter", "Position $position is out of bounds of fileNames list size: ${fileNames.size}")
        }
    }*/

    // 将数据绑定到 ViewHolder 上，用于显示特定位置的数据
    //主要的工作在这里
    /**
     * function:对所继承类的中的onBindViewHolder进行复写
     * 类成员：holder，RecyclerView.ViewHolder下的实例（代表当前正在绑定数据的列表项）
     *       position:位置信息
     * */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is HeaderViewHolder) {
            getSleepData(specifiedDate)?.let { sleepData ->
                holder.getupTime.text = sleepData.endTime
                holder.getupDate.text = sleepData.endDate
                holder.bedTime.text = sleepData.startTime
                holder.bedDate.text = sleepData.date

                // 检查 snoringDuration 是否为 null
                val snoringDuration = sleepData.snoringDuration
                val totalSeconds = if (snoringDuration != null) {
                    snoringDuration.toIntOrNull() ?: 0
                } else {
                    0
                }


               // val totalSeconds = sleepData.snoringDuration.toIntOrNull() ?: 0
                val hours = totalSeconds / 3600
                val remaining = totalSeconds % 3600
                val minutes = remaining / 60
                val seconds = remaining % 60
                val formattedTime = String.format("%d时%02d分%02d秒", hours, minutes, seconds)
                holder.totalSnoringTime.text = formattedTime

                // 实例化 SleepChartView 并传入指定日期
                //var sleepChartView = SleepChartView(holder.itemView.context, specifiedDate = specifiedDate)
                /*
                holder.sleepchart = sleepChartView
                holder.sleepchart.data = getWakeUpAndDeepSleep(holder.itemView.context,specifiedDate)
                holder.sleepchart.invalidate()*/

                val data1 = getWakeUpAndDeepSleep(context,specifiedDate)
                Log.d("dateSleep","data为：${data1}")

                val sleepChartView = holder.sleepchart
                val newData = getWakeUpAndDeepSleep(context, specifiedDate)
                sleepChartView.updateData(newData)

            }
        }

        else if (holder is ItemViewHolder) {
            val dataPosition = position - 1   // 减去头部视图的位置
            val displayName = displayFileNames[dataPosition]//表示播放文件名
            holder.snoringTime.text = displayName   //代表当前正在绑定数据的列表项

            val filePath = filePaths[dataPosition]
            // 加载波形数据（异步处理）
            loadWaveformAsync(filePath, holder)
            Log.d("sonringPos","dataPosition：${dataPosition}")
            Log.d("sonringPos","playingPosition：${playingPosition}")
            //根据当前播放状态设置icon（这段代码只是设计一个逻辑）
            if (dataPosition == playingPosition) {
                holder.player.setImageResource(R.drawable.icon_pause)
            } else {
                holder.player.setImageResource(R.drawable.icon_player)
            }


            holder.player.setOnClickListener {

                //stopPlaying()
                var pre_postion:Int = playingPosition
                //pre_postion?.let{notifyItemChanged(pre_postion!!)}
                if (playingPosition == dataPosition) {
                    // 如果当前点击的是正在播放的项，停止播放
                    stopPlaying()
                    playingPosition = -1
                    //notifyDataSetChanged()
                    Log.d("PositonPro","点击了正在播放的项，playingPosition设为-1")
                } else {
                    // 如果当前点击的不是正在播放的项，停止之前的播放，开始新的播放
                    stopPlaying()
                    Log.d("PositonPro","点击不同位置的项，dataPosotion为${dataPosition},playingPosition为${playingPosition}")
                    playingPosition = dataPosition
                    Log.d("PositonPro","现在的playingPosition为${playingPosition}")
                    //isPlaying.set(true)
                    Log.d("image","图片变换完成")
                    playPCMFileAsync(filePaths[dataPosition])
                }
                notifyDataSetChanged()

            }
        }
    }

    /*
    // 获取数据列表的大小，即 RecyclerView 中列表项的数量
    override fun getItemCount(): Int {
        Log.d("SnoringAdapter", "getItemCount called. Returning: ${fileNames.size}")
        return fileNames.size
    }*/

    // 获取数据列表的大小，即 RecyclerView 中列表项的数量，加上头部视图
    override fun getItemCount(): Int {
        return fileNames.size + 1 // 加上头部视图
    }

    // 获取指定位置的视图类型
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }
    /*
    // 定义 SnoringViewHolder 类，继承自 RecyclerView.ViewHolder，用于缓存列表项中的视图组件
    inner class SnoringViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 通过 findViewById 方法获取列表项中的 TextView 组件，用于显示文件名
        val snoringTime: TextView = itemView.findViewById(R.id.sonring_time)
        // 通过 findViewById 方法获取列表项中的 ImageButton 组件，可用于播放相关文件
        val player: ImageButton = itemView.findViewById(R.id.player)
        // 通过 findViewById 方法获取列表项中的 TextView 组件，用于显示时长
        val oneMinute: TextView = itemView.findViewById(R.id.oneminute)
    }*/

    // 头部视图的 ViewHolder
    //innner class表示一个内部类，定义在一个类的内部
    //可以访问外部类的成员，包括私有成员
    //创建内部类的实例时，需要一个外部类的实例作为上下文
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val totalSnoringTime: TextView = itemView.findViewById(R.id.snoringDuration)
        val getupTime: TextView = itemView.findViewById(R.id.getupTime)
        val getupDate: TextView = itemView.findViewById(R.id.getupDate)
        val bedTime: TextView = itemView.findViewById(R.id.bedTime)
        val bedDate: TextView = itemView.findViewById(R.id.bedDate)
        var sleepchart: SleepChartView = itemView.findViewById(R.id.SleepChart)
    }


    // 普通列表项的 ViewHolder
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 通过 findViewById 方法获取列表项中的 TextView 组件，用于显示文件名
        val snoringTime: TextView = itemView.findViewById(R.id.sonring_time)
        // 通过 findViewById 方法获取列表项中的 ImageButton 组件，可用于播放相关文件
        val player: ImageButton = itemView.findViewById(R.id.player)
        // 通过 findViewById 方法获取列表项中的 TextView 组件，用于显示时长
        val oneMinute: TextView = itemView.findViewById(R.id.oneminute)
        //波形图
        val waveformView: WaveformView = itemView.findViewById(R.id.waveformView)
        //seekbar进度条
        val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
    }


    // 异步播放音频文件，避免在主线程进行耗时操作
    private fun playPCMFileAsync(filePath: String) {
        isPlaying = true
        playbackThread = Thread {
            try {
                playPCMFile(filePath)
            } catch (e: Exception) {
                Log.e("SnoringAdapter", "播放异常: ${e.message}", e)
                stopPlaying()
                Handler(Looper.getMainLooper()).post {
                    val current = playingPosition
                    playingPosition = -1
                    notifyItemChanged(current)
                }
            }
        }
        playbackThread?.start()
    }

    // 播放 PCM 音频文件
    private fun playPCMFile(filePath: String) {
        val file = File(filePath)
        inputStream = FileInputStream(file)
        val bufferSize = AudioTrack.getMinBufferSize(
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        val data = ByteArray(bufferSize)
        audioTrack?.play()


        while (isPlaying) {
            val readBytes = inputStream?.read(data) ?: -1
            if (readBytes == -1) break
            audioTrack?.write(data, 0, readBytes)
        }


        stopPlaying()

    }





    /**
     * 结束录音
     * */
    private fun stopPlaying() {
        isPlaying = false
        try {
            playbackThread?.interrupt()
            playbackThread = null

            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }

            inputStream?.close()
        } catch (e: Exception) {
            Log.e("SnoringAdapter", "释放播放资源失败", e)
        } finally {
            audioTrack = null
            inputStream = null
        }
    }


    private fun loadWaveformAsync(filePath: String, holder: ItemViewHolder) {
        Thread {
            try {
                // 读取 PCM 数据
                val samples = readPcmSamples(filePath)
                // 更新 UI
                handler.post {
                    holder.waveformView.setSamples(samples)
                }
            } catch (e: Exception) {
                Log.e("Waveform", "加载波形失败: ${e.message}")
            }
        }.start()
    }

    private fun readPcmSamples(filePath: String): List<Short> {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(file.length().toInt())
        inputStream.read(buffer)
        inputStream.close()

        // 将字节转换为 Short 数组（16位 PCM）
        val samples = mutableListOf<Short>()
        for (i in 0 until buffer.size step 2) {
            val value = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
            samples.add(value.toShort())
        }
        return samples
    }

    /**
     * function:获取最近的sleepData
     * 该方法从SharedPreferences中读取存储的睡眠数据，通过遍历所有键值对找到最新的日期
     *然后根据该日期获取对应的睡眠数据，包括开始时间、结束时间、持续时间以及鼾声数据路径
     * return：最新的SleepData对象，如果没有找到数据则返回null
     * */
/*
    private fun getLatestSleepData(): SleepData? {
        //获取名为”SleepDataPrefs“的sharedPreferences
        val sharedPreferences = context.getSharedPreferences("SleepDataPrefs", Context.MODE_PRIVATE)
       //获取所有条目
        val allEntries = sharedPreferences.all
        //用于存储找到的最新日期
        var latestDate: String? = null

        // 遍历SharedPreferences中的所有键值对
        for (entry in allEntries) {

            if (Pattern.matches("\\d{4}-\\d{2}-\\d{2}", entry.key)) {
                if (latestDate == null || entry.key > latestDate) {
                    latestDate = entry.key
                }
            }
        }
        Log.d("SleepData", "latestDate: $latestDate")

        /**
         * 注：这的函数语法得好好看看
         * */

        return latestDate?.let { date ->
            try {
                // 获取对应日期的 JSON 字符串
                val jsonString = sharedPreferences.getString(date, null)
                if (jsonString != null) {
                    // 使用 Gson 解析 JSON 字符串
                    val gson = Gson()
                    val type = object : TypeToken<SleepData>() {}.type
                    val sleepData = gson.fromJson<SleepData>(jsonString, type)
                    Log.d("SleepData", "endTime: ${sleepData.endTime}")
                    Log.d("SleepData", "其所代表的键为: $date")
                    Log.d("SleepData", "end: ${sleepData.endDate}")

                    return@let sleepData
                }
            } catch (e: Exception) {
                Log.e("SleepData", "解析 JSON 数据出错: ${e.message}", e)
            }
            null

        }

    }*/

    private fun getSleepData(date: String?): SleepData? {
        val sharedPreferences = context.getSharedPreferences("SleepDataPrefs", Context.MODE_PRIVATE)
        val targetDate = date ?: getLatestDate(sharedPreferences)
        return targetDate?.let {
            try {
                val jsonString = sharedPreferences.getString(it, null)
                if (jsonString != null) {
                    val gson = Gson()
                    val type = object : TypeToken<SleepData>() {}.type
                    val sleepData = gson.fromJson<SleepData>(jsonString, type)
                    Log.d("SleepData", "endTime: ${sleepData.endTime}")
                    Log.d("SleepData", "其所代表的键为: $it")
                    Log.d("SleepData", "end: ${sleepData.endDate}")
                    sleepData
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("SleepData", "解析 JSON 数据出错: ${e.message}", e)
                null
            }
        }
    }

    //写一个能够得到指定日期的wakeUpAndDeepSleep的类

    private fun getLatestDate(sharedPreferences: SharedPreferences): String? {
        val allEntries = sharedPreferences.all
        var latestDate: String? = null
        for (entry in allEntries) {
            if (entry.key.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                if (latestDate == null || entry.key > latestDate) {
                    latestDate = entry.key
                }
            }
        }
        return latestDate
    }


    private fun getWakeUpAndDeepSleep(context: Context, specifiedDate: String? = null): wakeUpAndDeepSleep? {
        //获取名为”SleepDataPrefs“的sharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("WakeUpAndDeepSleep", Context.MODE_PRIVATE)
        //获取所有条目
        val allEntries = sharedPreferences.all
        //用于存储找到的最新日期
        var latestDate: String? = null
        var targetDate: String? = null

        // 遍历SharedPreferences中的所有键值对
        for (entry in allEntries) {
            if (Pattern.matches("\\d{4}-\\d{2}-\\d{2}", entry.key)) {
                if (latestDate == null || entry.key > latestDate) {
                    latestDate = entry.key
                }
            }
        }
        Log.d("wakeUpAndDeepSleep", "latestDate: $latestDate")

        // 判断是否传入了指定日期
        if (specifiedDate != null) {
            targetDate = specifiedDate
        } else {
            targetDate = latestDate
        }

        return targetDate?.let { date ->
            try {
                // 获取对应日期的 JSON 字符串
                val jsonString = sharedPreferences.getString(date, null)
                if (jsonString != null) {
                    // 使用 kotlinx.serialization 解析 JSON 字符串
                    val json = Json { ignoreUnknownKeys = true }
                    val result = json.decodeFromString<wakeUpAndDeepSleep>(jsonString)
                    Log.d("wakeUpAndDeepSleep", "Json解析成功")
                    return@let result
                }
            } catch (e: Exception) {
                Log.e("SleepData", "解析 JSON 数据出错: ${e.message}", e)
            }
            null
        }
    }


}