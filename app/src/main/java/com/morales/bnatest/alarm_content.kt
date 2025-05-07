package com.morales.bnatest

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class alarm_content(private val context: Context) {

    fun getAdjustedDayOfWeek(): String {
        // 获取 SharedPreferences
        val sharedPref: SharedPreferences = context.getSharedPreferences("alarm_config", Context.MODE_PRIVATE)
        val timeString = sharedPref.getString("timePicker", null)

        if (timeString.isNullOrEmpty()) {
            return "未设置时间"
        }

        // 解析时间，例如 "21:5"
        val (hour, minute) = timeString.split(":").map { it.toInt() }

        // 当前时间
        val now = Calendar.getInstance()

        // 构造目标时间
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果目标时间早于当前时间，加一天
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 获取调整后的星期几
        val dayOfWeek = alarmTime.get(Calendar.DAY_OF_WEEK)

        // 映射星期
        val weekdays = mapOf(
            Calendar.SUNDAY to "周日",
            Calendar.MONDAY to "周一",
            Calendar.TUESDAY to "周二",
            Calendar.WEDNESDAY to "周三",
            Calendar.THURSDAY to "周四",
            Calendar.FRIDAY to "周五",
            Calendar.SATURDAY to "周六"
        )

        return weekdays[dayOfWeek] ?: "未知"
    }

    /**
     * function:判断调整的日期是否在配置的集合中
     * @param：day 文本
     * @return：true或者false
     * */

    fun isWeekdayEnabled(day: String): Boolean {
        val sharedPref = context.getSharedPreferences("alarm_config", Context.MODE_PRIVATE)

        // 读取 set 类型的字符串集合（默认为空集合）
        val weekdaysSet = sharedPref.getStringSet("weekdays", emptySet()) ?: emptySet()

        return weekdaysSet.contains(day)
    }

    fun isOpenEnabled():Boolean{
        val sharedPref = context.getSharedPreferences("alarm_config", Context.MODE_PRIVATE)
        //读取isOpen的值
        val isOpen = sharedPref.getBoolean("isOpen",false)
        return isOpen
    }

    /**
     * function:得到当前时间与闹钟设置时间的差值
     * @param：无
     * @return：String
     * */

    fun getTimeDifferenceDescription(): String {
        val sharedPref = context.getSharedPreferences("alarm_config", Context.MODE_PRIVATE)
        val timeString = sharedPref.getString("timePicker", null) ?: return "未设置时间"

        // 解析时间字符串
        val (hour, minute) = timeString.split(":").map { it.toInt() }

        // 当前时间
        val now = Calendar.getInstance()

        // 构造目标时间
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果设置的时间早于当前时间，则加一天
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // 计算时间差（毫秒）
        val diffMillis = alarmTime.timeInMillis - now.timeInMillis

        val totalMinutes = diffMillis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return "距离闹钟开启：还有 $hours 小时 $minutes 分钟"
    }
}