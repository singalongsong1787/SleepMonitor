package com.morales.bnatest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morales.bnatest.R
import java.io.File
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.morales.bnatest.ui.home.HomeFragment

class alarm :AppCompatActivity(){

    private lateinit var recyclerViewWeekdays:RecyclerView
    private lateinit var recyvlerViewAwakes:RecyclerView
    private lateinit var weekdayAdapter: WeekdayAdapter
    private lateinit var weekdays:List<alarm_week>

    private lateinit var awakeList: List<alarm_awake>   //展示觉醒的时间间隔
    //时间选择器
    private lateinit var timePicker: TimePicker
    //闹钟是否打开
    private lateinit var isAlarmOpen:Switch
    //是否震动
    private lateinit var isAlarmVibration:Switch
    //是否唤醒
    private lateinit var isAlarmAwake:Switch
    //按钮设置
    private lateinit var icon_back:ImageButton
    //音量seekBar
    private lateinit var seekbar_volume:SeekBar
    //轻唤醒提前的分钟
    private  var pre_awakeMin:Int = 0


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_clock)

        //改变状态栏颜色
        val window: Window = window
        window.statusBarColor = ContextCompat.getColor(this, R.color.bar_alarmActivity)

        //初始化RecycleView
        recyclerViewWeekdays = findViewById(R.id.recycleView_week)//星期视图
        recyvlerViewAwakes = findViewById(R.id.recyclerView_awake)

        //初始化时间选择器
        timePicker = findViewById(R.id.timepicker)
        //Switch初始化
        isAlarmOpen =findViewById(R.id.isOpen_switch)
        isAlarmVibration = findViewById(R.id.swtich_vibration)
        isAlarmAwake = findViewById(R.id.switch_isAwake)

        //seekbar初始化
        seekbar_volume = findViewById(R.id.seekbar_volume)

        // 设置LayoutManager为LinearLayoutManager，并指定方向为水平
        recyclerViewWeekdays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyvlerViewAwakes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //按键初始化
        icon_back = findViewById(R.id.icon_back)
        icon_back.setOnClickListener{
            val intent =Intent(this,MainActivity::class.java)
            // 在 Intent 中附加额外数据，以键值对的形式存储，键为 "id"，值为 1
            intent.putExtra("id", 1)

            // 启动 MainActivity，传递上面创建的 Intent 对象
            startActivity(intent)
        }

        val Items = arrayOf("夏日之梦", "糖果", "元气满满","春日")
        //定位于bell TextView
        val bell_textView = findViewById<TextView>(R.id.name_bell)

        //闹钟是否开启
        //设置监听器

        /*
        //检查，没有初始化，有就显示
        val isContaintOpen:Boolean = checkKeyExistsAndIsTrue(this,"alarm_config","isOpen")
        if(isContaintOpen == true){
            //从配置文件中取出数值
            val openOrClose:Boolean = getIntFromSharedPreferencesBoolean(this,"alarm_config","isOpen")
            isAlarmOpen.isChecked = openOrClose
        }else{
            saveToSharedPreferences(this,"alarm_config","isOpen",false)
            isAlarmOpen.isChecked = false
        }

        //闹钟是否开启的Switch监听器
        isAlarmOpen.setOnCheckedChangeListener{buttonView,isChecked ->
            when(isChecked){
                true ->{
                    Log.d("awake","选择是")
                    //保存到闹钟配置文件中
                    saveToSharedPreferences(this,"alarm_config","isOpen",true)
                }
                false->{
                    Log.d("awake","选择否")
                    saveToSharedPreferences(this,"alarm_config","isOpen",false)
                }
            }
        }*/

        switch_init(isAlarmOpen,"isOpen")
        saveSwitchStates(isAlarmOpen,"isOpen")

        switch_init(isAlarmVibration,"isVibration")
        saveSwitchStates(isAlarmVibration,"isVibration")

        switch_init(isAlarmAwake,"isAwake")
        saveSwitchStates(isAlarmAwake,"isAwake")

        //初始化及恢复上一次的闹钟设置
        var isTimePicker = checkKeyExistsAndIsTrue(this,"alarm_config","timePicker")
        if(isTimePicker == true){   //非第一次情况
            //恢复显示上一次的时间
            val hour = getIntFromSharedPreferences(this,"alarm_config","timePicker_hour")
            if (hour != null) {
                timePicker.hour = hour
                Log.d("timePicker","恢复的时间hour为${hour}")
            }

            val minute = getIntFromSharedPreferences(this,"alarm_config","timePicker_min")
            if (minute != null) {
                timePicker.minute = minute
                Log.d("timePicker","恢复的时间minute为${minute}")
            }

        }
        //设置timePicker监听器
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            // 当时间改变时会触发此回调方法，弹出一个短暂的提示框，显示用户选择的时间
            //Toast.makeText(this, "您选择的时间是：$hourOfDay 时 $minute 分!", Toast.LENGTH_SHORT).show()
            var timeSelected:String = "${hourOfDay}:${minute}"

            Log.d("timePicker","选择的时间hour为${hourOfDay}")
            Log.d("timePicker","选择的时间minute为${minute}")

            saveToSharedPreferences(this,"alarm_config","timePicker",timeSelected)
            saveToSharedPreferences(this,"alarm_config","timePicker_hour",hourOfDay)
            saveToSharedPreferences(this,"alarm_config","timePicker_min",minute)
        }

        /*
        //保存switch方面信息
        isAlarmOpen.setOnClickListener{buttonView: CompoundButton, isChecked: Boolean  ->
            if(isChecked){
                saveToSharedPreferences()
            }

        }*/

        /***********音量seekbar的使用*************/
        val isExistsVolume = checkKeyExistsAndIsTrue(this,"alarm_config","volume")
        if(isExistsVolume == true){
            seekbar_volume.progress = getIntFromSharedPreferences(this,"alarm_config","volume")
        }else{
            seekbar_volume.progress = 50
        }


        seekbar_volume?.apply{
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                // 当 SeekBar 的进度发生变化时调用
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // 记录当前进度值和是否由用户操作引起
                    //Log.d("SeekBar", "拖动过程中的值：$progress, fromUser: $fromUser")
                }

                // 当用户开始拖动 SeekBar 时调用
                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // 记录开始滑动时的进度值
                    //Log.d("SeekBar", "开始滑动时的值：${seekBar.progress}")
                }

                // 当用户停止拖动 SeekBar 时调用
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // 记录停止滑动时的进度值
                    Log.d("SeekBar", "停止滑动时的值：${seekBar.progress}")
                    //将这个值写入配置文件中
                    //volume = seekBar.progress
                    saveToSharedPreferences(this@alarm,"alarm_config","volume",seekbar_volume.progress)
                }
            })
        }


        

        //检查文件是否存在，即是否为第一次使用，第一次使用就进行一个最原始的初始化
        var isexists = checkSharedPreferencesFileExists(this,"alarm_weekdays")
        Log.d("weekdays","文件是否存在：${isexists}")

        if(isexists == false){
            //准备信息
            val weekdayList = listOf(
                "周一" to false,
                "周二" to false,
                "周三" to false,
                "周四" to false,
                "周五" to false,
                "周六" to false,
                "周日" to false
            )
            // 存储数据到 SharedPreferences 的函数
            saveWeekdayData(this,weekdayList)
        }


        //初始化信息
        weekdays = listOf(
            alarm_week("周一",false),
            alarm_week("周二",false),
            alarm_week("周三",false),
            alarm_week("周四",false),
            alarm_week("周五",false),
            alarm_week("周六",false),
            alarm_week("周日",false)
        )

        //更新这个列表
        updateWeekdaysFromSharedPreferences(this,"alarm_weekdays",weekdays)

        //设置配置文件
        setWeekdaysToAlarmConfig(weekdays,this)


        //设置适配器
        recyclerViewWeekdays.adapter = WeekdayAdapter(weekdays,this)


        //设置name_bell(定义文本)
        var pos_bell:Int? = getIntFromSharedPreferences(this,"alarm_config","bell")
        pos_bell?.let{
            bell_textView.text = Items[pos_bell]
        }

        //awake部分设置适配器
        //初始化列表
        awakeList= listOf(
            alarm_awake(10,false),
            alarm_awake(20,false),
            alarm_awake(30,false)
        )


        recyvlerViewAwakes.adapter = AlarmAwakeAdapter(awakeList,this)
        Log.i("awaketime","${awakeList}")
        //创建或更新xml
        //查看是否含有键awakeTime
        var isAwakeTime:Boolean = checkKeyExistsAndIsTrue(this,"alarm_config","awakeTime")
        if(isAwakeTime == false){
            saveToSharedPreferences(this,"alarm_config","awakeTime","10")
        }

        /****铃声设计部分****/
        val bell_constraintLayout = findViewById<ConstraintLayout>(R.id.alarm_conslayout_4)
        bell_constraintLayout.setOnClickListener{
            // 定义对话框的列表项
            val items = arrayOf("夏日之梦", "糖果", "元气满满","春日")
            // 创建 AlertDialog.Builder 对象
            val builder = AlertDialog.Builder(this)
            // 设置对话框的标题
            builder.setTitle("铃声列表")

            // 设置对话框的列表项，并添加点击监听器
            builder.setItems(items) { dialog, which ->
                // 当用户点击列表中的某一项时，弹出一个 Toast 提示，显示点击的项的索引

                //更新文本
                updateTextViewText(bell_textView,items[which])
                //创建或者更新xml文件
                saveToSharedPreferences(this,"alarm_config","bell",which)

                Toast.makeText(this, "点击了${items[which]}", Toast.LENGTH_SHORT).show()
            }

            // 创建并显示对话框
            builder.create().show()
        }

    }




    /**
     * function:保存一个列表到xml文件中
     * @param:（1）上下文 （2）二元组列表
     * @return:无
     * */
    private fun saveWeekdayData(context: Context, data: List<Pair<String, Boolean>>) {
        // 获取 SharedPreferences 实例，文件名为 alarm_weekdays
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarm_weekdays", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // 遍历列表，将每个二元元组的数据存入 SharedPreferences
        for ((key, value) in data) {
            editor.putBoolean(key, value)
        }
        // 提交更改
        editor.apply()
    }

    /**
     * function:检查指定文件是否存在
     * @param：（1）上下文  （2）文件名
     * @return：Boolean
     * */
    private fun checkSharedPreferencesFileExists(context: Context, fileName: String): Boolean {
        // 获取 SharedPreferences 文件的存储目录
        val sharedPrefsDir = File(context.filesDir.parentFile, "shared_prefs")
        // 构建要检查的文件路径
        val sharedPrefsFile = File(sharedPrefsDir, "$fileName.xml")
        // 检查文件是否存在
        return sharedPrefsFile.exists()
    }

    /**
     * function:更新星期xml文件
     * @param：（1）上下文 （2）文件名 （3）星期列表
     * */
    private fun updateWeekdaysFromSharedPreferences(context: Context, fileName: String, weekdays: List<alarm_week>) {
        // 获取 SharedPreferences 实例
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        // 遍历 weekdays 列表
        for (weekday in weekdays) {
            // 从 SharedPreferences 中获取对应键的值，如果不存在则使用默认值 false
            val isSelected = sharedPreferences.getBoolean(weekday.name, false)
            // 更新 weekday 的 isSelected 属性
            weekday.isSelected = isSelected
        }
    }

    /**
     * function：更新文本
     * */
    private fun updateTextViewText(textView: TextView, newText: String) {
        textView.text = newText
        textView.textSize=20f
    }

    /**
     * function:对闹钟配置文件进行修改
     * @param:(1)文本 （2）key （3）value
     */
    private fun saveToSharedPreferences(context: Context, fileName: String, key: String, value: Any) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("不支持的数据类型: ${value.javaClass.name}")
        }

        editor.apply()
    }

    // 查找 Int 类型的值
    private fun getIntFromSharedPreferences(context: Context, fileName: String, key: String): Int {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getInt(key, 0)
        } else {
           50
        }
    }

    /**
     * function:查看一个文件是否包含对应键
     * @param:（1）上下文 （2）文件名 （3）键
     * @return：Boolean
     * */
    private fun checkKeyExistsAndIsTrue(context: Context, fileName: String, key: String): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPreferences.contains(key)
    }


    // 查找 布尔 类型的值
    private fun getIntFromSharedPreferencesBoolean(context: Context, fileName: String, key: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getBoolean(key, false)
        } else {
           false
        }
    }

    /**
     * funcition:对其alarm_config进行一个星期的配置
     * @param：（1）weekdays列表，保存alarm_week数据类
     *         (2)上下文
     * @return: mutableListOf<String>
     * */

    private fun setWeekdaysToAlarmConfig(weekdays:List<alarm_week>,context: Context){
        val selectedWeekdays = mutableListOf<String>()
        for (week in weekdays) {
            if (week.isSelected) {
                selectedWeekdays.add(week.name)
            }
        }

        val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarm_config", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 检查键 "weekdays" 是否存在，若存在则删除
        if (sharedPreferences.contains("weekdays")) {
            editor.remove("weekdays")
        }

        val set = selectedWeekdays.toSet()
        editor.putStringSet("weekdays", set)
        editor.apply()

    }


    /**
     * function:对Switch组件设置监听器，并能够将结果保存进配置文件中
     * @param：（1）Switch实例 （2）标签，放入的键
     * @return:无
     * */

    private fun saveSwitchStates(switch: Switch,keyTarget:String){
        switch.setOnCheckedChangeListener{buttonView,isChecked ->
            when(isChecked){
                true ->{

                    //保存到闹钟配置文件中
                    saveToSharedPreferences(this,"alarm_config",keyTarget,true)
                }
                false->{

                    saveToSharedPreferences(this,"alarm_config",keyTarget,false)
                }
            }
        }

    }

    /**
     * function：对Switch相关进行一个初始化
     * @param:（1）Switch的实例
     * */
    private fun switch_init(switch: Switch,keyTarget: String){
        //检查，没有初始化，有就显示
        val isContaintOpen:Boolean = checkKeyExistsAndIsTrue(this,"alarm_config",keyTarget)
        if(isContaintOpen == true){
            //从配置文件中取出数值
            val openOrClose:Boolean = getIntFromSharedPreferencesBoolean(this,"alarm_config",keyTarget)
            switch.isChecked = openOrClose
        }else{
            saveToSharedPreferences(this,"alarm_config","isOpen",false)
            switch.isChecked = false
        }

    }

}