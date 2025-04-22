package com.morales.bnatest

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AlarmAwakeAdapter(private val awakeList: List<alarm_awake>, private val context: Context) :
    RecyclerView.Adapter<AlarmAwakeAdapter.AwakeViewHolder>() {

    private val selectedColorResId = R.color.item_weekdays_selected
    private val unselectedColorResId = R.color.item_weekdays_unselceted


    //加载相关的视图
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AwakeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_item_awake, parent, false)
        Log.d("awakeday","成功")
        return AwakeViewHolder(view)
    }

    //绑定数据
    override fun onBindViewHolder(holder: AwakeViewHolder, position: Int) {
        val awake = awakeList[position]
        Log.d("awakeday","${position}")
        holder.timeTextView.text = "${awake.ntime}分钟"
        Log.d("awakeday","${awake.ntime}")

        if(holder.timeTextView.text == getStringValueFromSharedPreferences(context,"alarm_config","awakeTime")){
           holder.timeTextView.setTextColor(ContextCompat.getColor(holder.timeTextView.context, selectedColorResId))
        }
        else{
            holder.timeTextView.setTextColor(ContextCompat.getColor(holder.timeTextView.context, unselectedColorResId))
        }
    }

    override fun getItemCount(): Int {
        Log.d("awakeday","${awakeList.size}")
        return awakeList.size
    }

    //内部嵌置类，展示textView,一般包含item的各个控件和操作
    inner class AwakeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.item_awake_time)

        init{

            itemView.setOnClickListener{
                //更新闹钟配置文件alarm_config.xml
                saveToSharedPreferences(context,"alarm_config","awakeTime",timeTextView.text)
                notifyDataSetChanged()
            }

        }

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

    private fun getStringValueFromSharedPreferences(context: Context, fileName: String, key: String): String? {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

}