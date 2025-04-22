package com.morales.bnatest

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView


/**
 * 构造函数：alarm_week列表   监听器
 * */
class WeekdayAdapter(private var weekdays: List<alarm_week>, private val context: Context) :
    RecyclerView.Adapter<WeekdayAdapter.WeekdayViewHolder>() {

    interface  OnWeekdayClickListener {
        fun onWeekdayClick(position: Int)
    }

    private val selectedColorResId = R.color.item_weekdays_selected
    private val unselectedColorResId = R.color.item_weekdays_unselceted

    //创建ViewHolder函数
    // 创建ViewHolder的函数
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekdayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_item_week_day, parent, false)
        return WeekdayViewHolder(view)
    }

    //绑定数据
    override fun onBindViewHolder(holder: WeekdayViewHolder, position: Int) {
        // 获取当前位置的Weekday对象
        val weekday = weekdays[position]
        // 设置星期名称
        holder.textViewWeekday.text = weekday.name
        // 设置选中状态
        holder.textViewWeekday.isSelected = weekday.isSelected
        holder.itemView.isSelected = weekday.isSelected

        //根据选中状态设置颜色
        setTextViewColor(holder.textViewWeekday, weekday.isSelected)
    }

    // 获取数据项数量的函数
    override fun getItemCount() = weekdays.size

    //ViewHolder内部类
    inner class WeekdayViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

        //定义textView变量
        val textViewWeekday:TextView = itemView.findViewById(R.id.item_one_day)

        init{
            itemView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val weekday = weekdays[position]
                    // 取反 isSelected 属性
                    weekday.isSelected =!weekday.isSelected
                    // 更新 UI
                    notifyItemChanged(position)
                    updateSharedPreferences()
                }
            }
        }

    }

    // 根据选中状态设置 TextView 颜色的方法
    private fun setTextViewColor(textView: TextView, isSelected: Boolean) {
        val colorResId = if (isSelected) selectedColorResId else unselectedColorResId
        textView.setTextColor(ContextCompat.getColor(textView.context, colorResId))
    }

    private fun updateSharedPreferences() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarm_weekdays", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (weekday in weekdays) {
            editor.putBoolean(weekday.name, weekday.isSelected)
        }
        editor.apply()
    }


    }